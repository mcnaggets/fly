package by.fly.service;

import by.fly.model.PrinterType;
import by.fly.model.Settings;
import by.fly.repository.SettingsRepository;
import com.mysema.query.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;

    public Settings findOne(String settingName) {
        return settingsRepository.findOne(settingName);
    }

    public List<String> getItemTypes() {
        Settings itemType = settingsRepository.findOne(Settings.ITEM_TYPES);
        if (itemType == null) {
            final List<String> types = Arrays.stream(PrinterType.values()).map(PrinterType::getMessage).collect(Collectors.toList());
            settingsRepository.save(itemType = new Settings(Settings.ITEM_TYPES, types));
        }
        return (List<String>) itemType.getValue();
    }

    public void save(Settings setting) {
        settingsRepository.save(setting);
    }

    public Settings findOne(Predicate predicate) {
        return settingsRepository.findOne(predicate);
    }
}
