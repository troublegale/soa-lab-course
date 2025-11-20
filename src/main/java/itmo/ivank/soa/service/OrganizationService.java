package itmo.ivank.soa.service;

import itmo.ivank.soa.entity.Organization;
import itmo.ivank.soa.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public Organization getById(Long id) {
        return organizationRepository.findById(id).orElseThrow();
    }

}
