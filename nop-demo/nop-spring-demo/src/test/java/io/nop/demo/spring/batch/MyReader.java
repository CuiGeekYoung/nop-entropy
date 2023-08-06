package io.nop.demo.spring.batch;

import io.nop.demo.spring.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyReader implements ItemReader {
    static final Logger LOG = LoggerFactory.getLogger(MyReader.class);
    @Autowired
    SysUserMapper sysUserMapper;

    @Override
    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        LOG.info("*********before read*************");
        try {
            return MybatisFix.runWithoutTransaction(() -> {
                return sysUserMapper.selectUserByUserName("a");
            });
        } finally {
            LOG.info("###########after read###############");
        }
    }
}