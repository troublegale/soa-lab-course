package itmo.ivank.soa.util;

import itmo.ivank.soa.dto.OrganizationQuery;
import itmo.ivank.soa.dto.filter.AddressFilter;
import itmo.ivank.soa.dto.filter.CoordinatesFilter;
import itmo.ivank.soa.dto.filter.LocationFilter;
import itmo.ivank.soa.dto.filter.primitive.DateFilter;
import itmo.ivank.soa.dto.filter.primitive.NumberFilter;
import itmo.ivank.soa.dto.filter.primitive.StringFilter;
import itmo.ivank.soa.dto.filter.primitive.TypeFilter;
import itmo.ivank.soa.entity.Organization;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuilder {

    public static Specification<Organization> buildSpecification(OrganizationQuery query) {
        Specification<Organization> spec = Specification.unrestricted();
        spec = addNumberFilter(spec, "id", query.idFilter());
        spec = addStringFilter(spec, "name", query.nameFilter());
        spec = addCoordinatesFilter(spec, query.coordinatesFilter());
        spec = addDateFilter(spec, "creationDate",  query.creationDateFilter());
        spec = addNumberFilter(spec, "annualTurnover", query.annualTurnoverFilter());
        spec = addStringFilter(spec, "fullName", query.fullNameFilter());
        spec = addTypeFilter(spec, "type", query.typeFilter());
        spec = addAddressFilter(spec, query.officialAddressFilter());
        return spec;
    }

    private static <T extends Number> Specification<Organization> addNumberFilter(Specification<Organization> spec, String fieldPath, NumberFilter<T> filter) {
        if (filter == null) return spec;
        if (filter.eq() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(resolvePath(root, fieldPath), filter.eq()));
        }
        if (filter.gt() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.gt(resolvePath(root, fieldPath), filter.gt()));
        }
        if (filter.ge() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.ge(resolvePath(root, fieldPath), filter.ge()));
        }
        if (filter.lt() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lt(resolvePath(root, fieldPath), filter.lt()));
        }
        if (filter.le() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.le(resolvePath(root, fieldPath), filter.le()));
        }
        return spec;
    }

    private static Specification<Organization> addStringFilter(Specification<Organization> spec, String fieldPath, StringFilter filter) {
        if (filter == null) return spec;
        if (filter.eq() != null && !filter.eq().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(resolvePath(root, fieldPath), filter.eq()));
        }
        if (filter.contains() != null && !filter.contains().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(resolvePath(root, fieldPath), "%" + filter.contains() + "%"));
        }
        if (filter.startsWith() != null && !filter.startsWith().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(resolvePath(root, fieldPath), filter.startsWith() + "%"));
        }
        if (filter.endsWith() != null && !filter.endsWith().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(resolvePath(root, fieldPath), "%" + filter.endsWith()));
        }
        return spec;
    }

    @SuppressWarnings("SameParameterValue")
    private static Specification<Organization> addDateFilter(Specification<Organization> spec, String fieldPath, DateFilter filter) {
        if (filter == null) return spec;
        if (filter.eq() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(resolvePath(root, fieldPath), filter.eq()));
        }
        if (filter.before() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(resolvePath(root, fieldPath), filter.before()));
        }
        if (filter.after() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThan(resolvePath(root, fieldPath), filter.after()));
        }
        return spec;
    }

    @SuppressWarnings("SameParameterValue")
    private static Specification<Organization> addTypeFilter(Specification<Organization> spec, String fieldPath, TypeFilter filter) {
        if (filter == null) return spec;
        if (filter.eq() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(resolvePath(root, fieldPath), filter.eq()));
        }
        if (filter.in() != null && !filter.in().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    resolvePath(root, fieldPath).in(filter.in()));
        }
        return spec;
    }

    private static Specification<Organization> addCoordinatesFilter(Specification<Organization> spec, CoordinatesFilter filter) {
        if (filter == null) return spec;
        spec = addNumberFilter(spec, "coordinates.x", filter.xFilter());
        spec = addNumberFilter(spec, "coordinates.y", filter.yFilter());
        return spec;
    }

    private static Specification<Organization> addAddressFilter(Specification<Organization> spec, AddressFilter filter) {
        if (filter == null) return spec;
        spec = addStringFilter(spec, "officialAddress.street", filter.streetFilter());
        spec = addLocationFilter(spec, filter.townFilter());
        return spec;
    }

    private static Specification<Organization> addLocationFilter(Specification<Organization> spec, LocationFilter filter) {
        if (filter == null) return spec;
        spec = addNumberFilter(spec, "officialAddress.town.x", filter.xFilter());
        spec = addNumberFilter(spec, "officialAddress.town.y", filter.yFilter());
        spec = addStringFilter(spec, "officialAddress.town.name", filter.nameFilter());
        return spec;
    }

    @SuppressWarnings("unchecked")
    private static <T> Expression<T> resolvePath(Root<?> root, String fieldPath) {
        Path<?> path = root;
        for (String part : fieldPath.split("\\.")) {
            path = path.get(part);
        }
        return (Expression<T>) path;
    }

}
