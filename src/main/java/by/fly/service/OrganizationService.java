package by.fly.service;

import by.fly.model.Organization;
import by.fly.model.QOrganization;
import by.fly.repository.OrganizationRepository;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

@Service
@Transactional
public class OrganizationService {

    private static final Logger LOGGER = getLogger(OrganizationService.class);

    private static final String LOGO_FILE = "logo.file";

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

    public boolean saveOrganizationLogo(Organization organization, File file) {
        try {
            gridFsOperations.delete(query(whereFilename().is(getLogoFileName(organization))));
            return gridFsOperations.store(
                    Files.newInputStream(file.toPath()),
                    getLogoFileName(organization),
                    Files.probeContentType(file.toPath())).getId() != null;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private String getLogoFileName(Organization organization) {
        return QOrganization.organization.toString() + File.separator + organization.getName() + File.separator + LOGO_FILE;
    }

    public InputStream findOrganizationLogo(Organization organization) {
        GridFSDBFile file = gridFsOperations.findOne(query(whereFilename().is(getLogoFileName(organization))));
        return file != null ? file.getInputStream() : null;
    }
}
