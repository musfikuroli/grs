package com.grs.core.dao;

import com.grs.core.domain.grs.GeneralSettings;
import com.grs.core.repo.grs.GeneralSettingsRepo;
import com.grs.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneralSettingsDAO {
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    public GeneralSettings findByName(String name) {
        return generalSettingsRepo.findByName(name);
    }

    public GeneralSettings save(GeneralSettings generalSettings) {
        return generalSettingsRepo.save(generalSettings);
    }

    public String getValueByFieldName(String name) {
        GeneralSettings fileSizeSetting = generalSettingsRepo.findByName(name);
        if(fileSizeSetting == null || fileSizeSetting.getStatus().equals(false)) {
            return null;
        }
        return fileSizeSetting.getValue();
    }

    public String getAllowedFileTypes() {
        return getValueByFieldName(Constant.fileTypeFieldName);
    }

    public Integer getMaximumFileSize() {
        Integer maxFileSize = 0;
        String maxFileSizeStr = getValueByFieldName(Constant.fileSizeFieldName);
        if(maxFileSize != null) {
            maxFileSize = Integer.parseInt(maxFileSizeStr);
        }
        return maxFileSize;
    }
}

