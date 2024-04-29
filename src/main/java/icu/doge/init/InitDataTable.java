package icu.doge.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


@Component
public class InitDataTable implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public InitDataTable(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        executeInitSql();
        checkPath();
    }

    private void executeInitSql() {
        try {
            // Load init.sql from resources
            ClassPathResource resource = new ClassPathResource("sql/init.sql");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            String initSql = FileCopyUtils.copyToString(reader);

            // Execute init.sql using JdbcTemplate
            jdbcTemplate.execute(initSql);

            System.out.println("init.sql executed successfully.");
        } catch (IOException e) {
            System.err.println("Error executing init.sql: " + e.getMessage());
        }
    }

    private void checkPath() {
        File folder = new File("pic");
        if (!folder.exists() && !folder.isDirectory()) {
            boolean mkdirred = folder.mkdir();
            if (mkdirred){
                System.out.println("创建文件夹");
            }else {
                System.out.println("创建文件夹失败");
            }

        } else {
            System.out.println("文件夹已存在");
        }

    }
}
