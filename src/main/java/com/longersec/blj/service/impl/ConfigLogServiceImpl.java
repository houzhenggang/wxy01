package com.longersec.blj.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.longersec.blj.dao.ConfigLogDao;
import com.longersec.blj.domain.ConfigLog;
import com.longersec.blj.service.ConfigLogService;


@Transactional
@Service
public class ConfigLogServiceImpl implements ConfigLogService {

    @Autowired
    private ConfigLogDao ConfigLogDao;

    @Override
    public boolean editConfigLog(ConfigLog configLog) {
        // TODO Auto-generated method stub
        return this.ConfigLogDao.editConfigLog(configLog);
    }

    @Override
    public boolean addConfigLog(ConfigLog configLog) {
        // TODO Auto-generated method stub
        return this.ConfigLogDao.addConfigLog(configLog);
    }

    @Override
    public boolean delConfigLog(List<Integer> ids) {
        // TODO Auto-generated method stub
        return this.ConfigLogDao.delConfigLog(ids);
    }

    @Override
    public List<Object> findAll(ConfigLog configLog, int page_start, int page_length) {
        return ConfigLogDao.findAll(configLog, page_start, page_length);
    }

    @Override
    public int selectCount() {
        return ConfigLogDao.selectCount();
    }
    @Override
    public int checkname(Integer id, String name) {
        return ConfigLogDao.checkname(id,name);
    }

    @Override
    public int checksort(Integer id, Integer sort) {
        return ConfigLogDao.checksort(id,sort);
    }
}

