package by.fly.service;

import by.fly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    public static final String MASTER_CODE_PREFIX = "*M";

    @Autowired
    private UserRepository repository;

    public String generateBarcode() {
        final long userIndex = repository.count() + 1;
        String barcode = new StringBuilder().append(MASTER_CODE_PREFIX).append(userIndex).toString();
        int i = 0;
        while (repository.findByBarcode(barcode) != null) {
            barcode = new StringBuilder().append(MASTER_CODE_PREFIX).append(userIndex).append('_').append(++i).toString();
        }
        return barcode;
    }

}
