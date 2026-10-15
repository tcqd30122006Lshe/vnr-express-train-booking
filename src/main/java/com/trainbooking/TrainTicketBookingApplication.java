package com.trainbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrainTicketBookingApplication {

    public static void main(String[] args) {
        configureCloudDatabase();
        SpringApplication.run(TrainTicketBookingApplication.class, args);
    }

    private static void configureCloudDatabase() {
        System.out.println("==================================================================");
        System.out.println("🚀 [VNR EXPRESS] CHECKING DATABASE ENVIRONMENT CONFIGURATION...");

        String mysqlUrl = System.getenv("MYSQL_URL");
        if (mysqlUrl == null || mysqlUrl.isBlank()) {
            mysqlUrl = System.getenv("DATABASE_URL");
        }
        if (mysqlUrl == null || mysqlUrl.isBlank()) {
            mysqlUrl = System.getenv("MYSQL_PUBLIC_URL");
        }

        if (mysqlUrl != null && !mysqlUrl.isBlank()) {
            mysqlUrl = mysqlUrl.trim();
            if (mysqlUrl.startsWith("jdbc:mysql://")) {
                System.setProperty("spring.datasource.url", mysqlUrl);
                System.out.println(">>> Direct JDBC URL detected: " + mysqlUrl.replaceAll(":[^:@]+@", ":****@"));
            } else if (mysqlUrl.startsWith("mysql://")) {
                try {
                    System.out.println(">>> Found cloud database URL: " + mysqlUrl.replaceAll(":[^:@]+@", ":****@"));
                    java.net.URI uri = new java.net.URI(mysqlUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                    String path = uri.getPath();
                    if (path != null && path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    if (path == null || path.isBlank()) {
                        path = "railway";
                    }

                    String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + path
                            + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                    System.setProperty("spring.datasource.url", jdbcUrl);
                    System.out.println(">>> Auto-configured JDBC URL: " + jdbcUrl);

                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        System.setProperty("spring.datasource.username", parts[0]);
                        System.setProperty("spring.datasource.password", parts[1]);
                        System.out.println(">>> Auto-configured DB Username: " + parts[0]);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to parse MYSQL_URL: " + e.getMessage());
                }
            }
        }

        // Fix if user provided SPRING_DATASOURCE_URL starting with mysql:// instead of jdbc:mysql://
        String springDsUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (springDsUrl != null && springDsUrl.startsWith("mysql://")) {
            String fixed = "jdbc:" + springDsUrl;
            System.setProperty("spring.datasource.url", fixed);
            System.out.println(">>> Auto-fixed SPRING_DATASOURCE_URL prefix to: " + fixed.replaceAll(":[^:@]+@", ":****@"));
        }

        // Warning if Railway template variables were pasted literally without resolution
        String mysqlHost = System.getenv("MYSQLHOST");
        if (mysqlHost != null && mysqlHost.contains("${{")) {
            System.err.println("⚠️ WARNING: MYSQLHOST contains unexpanded template: '" + mysqlHost + "'. In Railway, use '${{MySQL.RAILWAY_PRIVATE_DOMAIN}}' or the exact domain like 'mysql.railway.internal'!");
        }

        System.out.println(">>> Effective spring.datasource.url: " + (System.getProperty("spring.datasource.url") != null ? System.getProperty("spring.datasource.url") : System.getenv("SPRING_DATASOURCE_URL")));
        System.out.println(">>> Effective MYSQLHOST: " + mysqlHost);
        System.out.println(">>> Effective MYSQLPORT: " + System.getenv("MYSQLPORT"));
        System.out.println("==================================================================");
    }
}
