package by.fly.service;

import by.fly.model.Organization;
import by.fly.repository.OrganizationRepository;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

@Service
@Transactional
public class OrganizationService {

    private static final Logger LOGGER = getLogger(OrganizationService.class);

    private static final String LOGO_FILE = "logo.file";
    private static final String ORGANIZATION = "organization";

    @Autowired
    private OrganizationRepository repository;

    @Autowired
    private GridFsOperations gridFsOperations;

    public Organization getRootOrganization() {
        if (repository.count() > 0) {
            return repository.findAll().iterator().next();
        }
        Organization organization = new Organization("ЦЗК");
        return repository.save(organization);
    }

    public boolean setOrganizationLogo(Organization organization, InputStream fileStream) {
        return gridFsOperations.store(fileStream, getLogoFileName(organization)).getId() != null;
    }

    private String getLogoFileName(Organization organization) {
        return ORGANIZATION + File.separator + organization.getName() + File.separator + LOGO_FILE;
    }

    public InputStream getOrganizationLogo(Organization organization) {
        GridFSDBFile file = gridFsOperations.findOne(query(whereFilename().is(getLogoFileName(organization))));
        return file != null ? file.getInputStream() : null;
    }
}
