package itmo.ivank.ejb.service;

import itmo.ivank.ejb.dto.*;
import itmo.ivank.ejb.dto.filter.*;
import itmo.ivank.ejb.entity.Employee;
import itmo.ivank.ejb.entity.Organization;
import itmo.ivank.ejb.entity.OrganizationType;
import itmo.ivank.ejb.exception.InvalidSearchQueryException;
import itmo.ivank.ejb.exception.NotFoundException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class OrganizationServiceBean implements OrganizationServiceRemote {

    @PersistenceContext(unitName = "soaPU")
    private EntityManager em;

    @Override
    public Organization getById(Long id) {
        Organization org = em.find(Organization.class, id);
        if (org == null) {
            throw new NotFoundException("Organization not found: " + id);
        }
        return org;
    }

    @Override
    public OrganizationsPage getAll(Integer page, Integer size) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Organization.class)));
        Long total = em.createQuery(countQuery).getSingleResult();

        // Data query
        CriteriaQuery<Organization> dataQuery = cb.createQuery(Organization.class);
        Root<Organization> root = dataQuery.from(Organization.class);
        dataQuery.select(root).orderBy(cb.asc(root.get("id")));

        TypedQuery<Organization> query = em.createQuery(dataQuery);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        List<Organization> orgs = query.getResultList();
        int totalPages = (int) Math.ceil((double) total / size);

        return new OrganizationsPage(orgs, page, size, total, totalPages);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Organization create(OrganizationRequest dto) {
        Organization org = Organization.builder()
                .name(dto.name())
                .coordinates(dto.coordinates())
                .annualTurnover(dto.annualTurnover())
                .fullName(dto.fullName())
                .type(dto.type())
                .officialAddress(dto.officialAddress())
                .creationDate(LocalDate.now())
                .build();
        em.persist(org);
        return org;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Organization createRaw(OrganizationRequest dto) {
        // Insert with explicit ID (for compensation)
        em.createNativeQuery("""
            INSERT INTO organizations (
                id, name, creation_date, annual_turnover, full_name,
                coordinates_x, coordinates_y, type,
                official_address_street, official_address_town_x,
                official_address_town_y, official_address_town_name
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)
                .setParameter(1, dto.id())
                .setParameter(2, dto.name())
                .setParameter(3, dto.creationDate())
                .setParameter(4, dto.annualTurnover())
                .setParameter(5, dto.fullName())
                .setParameter(6, dto.coordinates() != null ? dto.coordinates().getX() : null)
                .setParameter(7, dto.coordinates() != null ? dto.coordinates().getY() : null)
                .setParameter(8, dto.type() != null ? dto.type().name() : null)
                .setParameter(9, dto.officialAddress() != null ? dto.officialAddress().getStreet() : null)
                .setParameter(10, dto.officialAddress() != null && dto.officialAddress().getTown() != null ? dto.officialAddress().getTown().getX() : null)
                .setParameter(11, dto.officialAddress() != null && dto.officialAddress().getTown() != null ? dto.officialAddress().getTown().getY() : null)
                .setParameter(12, dto.officialAddress() != null && dto.officialAddress().getTown() != null ? dto.officialAddress().getTown().getName() : null)
                .executeUpdate();

        return em.find(Organization.class, dto.id());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Organization update(Long id, OrganizationRequest dto) {
        Organization org = em.find(Organization.class, id);
        if (org == null) {
            throw new NotFoundException("Organization not found: " + id);
        }
        org.setName(dto.name());
        org.setCoordinates(dto.coordinates());
        org.setAnnualTurnover(dto.annualTurnover());
        org.setFullName(dto.fullName());
        org.setType(dto.type());
        org.setOfficialAddress(dto.officialAddress());
        return em.merge(org);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Long id) {
        Organization org = em.find(Organization.class, id);
        if (org == null) {
            throw new NotFoundException("Organization not found: " + id);
        }
        em.remove(org);
    }

    @Override
    public EmployeesList getEmployees(Long id) {
        Organization org = getById(id);
        List<Employee> employees = em.createQuery(
                        "SELECT e FROM Employee e WHERE e.organization = :org", Employee.class)
                .setParameter("org", org)
                .getResultList();
        return new EmployeesList(employees);
    }

    @Override
    public TurnoverResponse getTotalTurnover() {
        List<Organization> orgs = em.createQuery(
                "SELECT o FROM Organization o", Organization.class).getResultList();
        Double total = orgs.stream()
                .map(org -> Double.valueOf(org.getAnnualTurnover()))
                .reduce(0.0, Double::sum);
        return new TurnoverResponse(total, orgs.size());
    }

    @Override
    public TypeCountResponse getOrganizationTypesCount() {
        List<TypeCount> typeCounts = new ArrayList<>();
        for (OrganizationType type : OrganizationType.values()) {
            Long count = em.createQuery(
                            "SELECT COUNT(o) FROM Organization o WHERE o.type = :type", Long.class)
                    .setParameter("type", type)
                    .getSingleResult();
            typeCounts.add(new TypeCount(type, count.intValue()));
        }
        return new TypeCountResponse(typeCounts);
    }

    @Override
    public OrganizationsPage getFiltered(Integer page, Integer size, OrganizationQuery queryDto) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            // Count query
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Organization> countRoot = countQuery.from(Organization.class);
            Predicate countPredicate = buildPredicate(cb, countRoot, queryDto);
            countQuery.select(cb.count(countRoot)).where(countPredicate);
            Long total = em.createQuery(countQuery).getSingleResult();

            // Data query
            CriteriaQuery<Organization> dataQuery = cb.createQuery(Organization.class);
            Root<Organization> root = dataQuery.from(Organization.class);
            Predicate predicate = buildPredicate(cb, root, queryDto);
            dataQuery.select(root).where(predicate);

            // Sorting
            List<Order> orders = buildOrders(cb, root, queryDto.sort());
            if (orders.isEmpty()) {
                orders.add(cb.asc(root.get("id")));
            }
            dataQuery.orderBy(orders);

            TypedQuery<Organization> query = em.createQuery(dataQuery);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);

            List<Organization> orgs = query.getResultList();
            int totalPages = (int) Math.ceil((double) total / size);

            return new OrganizationsPage(orgs, page, size, total, totalPages);
        } catch (Exception e) {
            throw new InvalidSearchQueryException("Invalid filters or sorting parameters: " + e.getMessage());
        }
    }

    @Override
    public OrganizationsPage getOrganizationsLessThanFullName(String value, Integer page, Integer size) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Organization> countRoot = countQuery.from(Organization.class);
        countQuery.select(cb.count(countRoot)).where(cb.lessThan(countRoot.get("fullName"), value));
        Long total = em.createQuery(countQuery).getSingleResult();

        // Data query
        CriteriaQuery<Organization> dataQuery = cb.createQuery(Organization.class);
        Root<Organization> root = dataQuery.from(Organization.class);
        dataQuery.select(root).where(cb.lessThan(root.get("fullName"), value));

        TypedQuery<Organization> query = em.createQuery(dataQuery);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        List<Organization> orgs = query.getResultList();
        int totalPages = (int) Math.ceil((double) total / size);

        return new OrganizationsPage(orgs, page, size, total, totalPages);
    }

    // ========== Helper methods for building predicates ==========

    private Predicate buildPredicate(CriteriaBuilder cb, Root<Organization> root, OrganizationQuery q) {
        List<Predicate> predicates = new ArrayList<>();

        addNumberFilter(cb, root, "id", q.idFilter(), predicates);
        addStringFilter(cb, root, "name", q.nameFilter(), predicates);
        addDateFilter(cb, root, "creationDate", q.creationDateFilter(), predicates);
        addNumberFilter(cb, root, "annualTurnover", q.annualTurnoverFilter(), predicates);
        addStringFilter(cb, root, "fullName", q.fullNameFilter(), predicates);
        addTypeFilter(cb, root, "type", q.typeFilter(), predicates);

        if (q.coordinatesFilter() != null) {
            addNumberFilter(cb, root, "coordinates.x", q.coordinatesFilter().xFilter(), predicates);
            addNumberFilter(cb, root, "coordinates.y", q.coordinatesFilter().yFilter(), predicates);
        }

        if (q.officialAddressFilter() != null) {
            addStringFilter(cb, root, "officialAddress.street", q.officialAddressFilter().streetFilter(), predicates);
            if (q.officialAddressFilter().townFilter() != null) {
                addNumberFilter(cb, root, "officialAddress.town.x", q.officialAddressFilter().townFilter().xFilter(), predicates);
                addNumberFilter(cb, root, "officialAddress.town.y", q.officialAddressFilter().townFilter().yFilter(), predicates);
                addStringFilter(cb, root, "officialAddress.town.name", q.officialAddressFilter().townFilter().nameFilter(), predicates);
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    @SuppressWarnings("unchecked")
    private <T extends Number & Comparable<T>> void addNumberFilter(CriteriaBuilder cb, Root<Organization> root,
                                                     String fieldPath, NumberFilter<T> filter, List<Predicate> predicates) {
        if (filter == null) return;
        Path<T> path = resolvePath(root, fieldPath);
        if (filter.eq() != null) {
            predicates.add(cb.equal(path, filter.eq()));
        }
        if (filter.gt() != null) {
            predicates.add(cb.greaterThan(path, filter.gt()));
        }
        if (filter.ge() != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, filter.ge()));
        }
        if (filter.lt() != null) {
            predicates.add(cb.lessThan(path, filter.lt()));
        }
        if (filter.le() != null) {
            predicates.add(cb.lessThanOrEqualTo(path, filter.le()));
        }
    }

    private void addStringFilter(CriteriaBuilder cb, Root<Organization> root,
                                  String fieldPath, StringFilter filter, List<Predicate> predicates) {
        if (filter == null) return;
        Path<String> path = resolvePath(root, fieldPath);
        if (filter.eq() != null && !filter.eq().isBlank()) {
            predicates.add(cb.equal(path, filter.eq().trim()));
        }
        if (filter.contains() != null && !filter.contains().isBlank()) {
            predicates.add(cb.like(path, "%" + escapeLike(filter.contains().trim()) + "%", '\\'));
        }
        if (filter.startsWith() != null && !filter.startsWith().isBlank()) {
            predicates.add(cb.like(path, escapeLike(filter.startsWith().trim()) + "%", '\\'));
        }
        if (filter.endsWith() != null && !filter.endsWith().isBlank()) {
            predicates.add(cb.like(path, "%" + escapeLike(filter.endsWith().trim()), '\\'));
        }
    }

    private void addDateFilter(CriteriaBuilder cb, Root<Organization> root,
                                String fieldPath, DateFilter filter, List<Predicate> predicates) {
        if (filter == null) return;
        Path<LocalDate> path = resolvePath(root, fieldPath);
        if (filter.eq() != null) {
            predicates.add(cb.equal(path, filter.eq()));
        }
        if (filter.before() != null) {
            predicates.add(cb.lessThan(path, filter.before()));
        }
        if (filter.after() != null) {
            predicates.add(cb.greaterThan(path, filter.after()));
        }
    }

    private void addTypeFilter(CriteriaBuilder cb, Root<Organization> root,
                                String fieldPath, TypeFilter filter, List<Predicate> predicates) {
        if (filter == null) return;
        Path<OrganizationType> path = resolvePath(root, fieldPath);
        if (filter.eq() != null) {
            predicates.add(cb.equal(path, filter.eq()));
        }
        if (filter.in() != null && !filter.in().isEmpty()) {
            predicates.add(path.in(filter.in()));
        }
    }

    private List<Order> buildOrders(CriteriaBuilder cb, Root<Organization> root, List<String> sortParams) {
        List<Order> orders = new ArrayList<>();
        if (sortParams == null || sortParams.isEmpty()) {
            return orders;
        }
        for (String sortParam : sortParams) {
            boolean desc = sortParam.startsWith("-");
            String field = desc ? sortParam.substring(1) : sortParam;
            Path<?> path = resolvePath(root, field);
            orders.add(desc ? cb.desc(path) : cb.asc(path));
        }
        return orders;
    }

    @SuppressWarnings("unchecked")
    private <T> Path<T> resolvePath(Root<?> root, String fieldPath) {
        Path<?> path = root;
        for (String part : fieldPath.split("\\.")) {
            path = path.get(part);
        }
        return (Path<T>) path;
    }

    private String escapeLike(String input) {
        if (input == null) return null;
        return input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

}
