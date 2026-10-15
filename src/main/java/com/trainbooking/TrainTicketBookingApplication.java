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
        if (mysqlUrl == null || mysqlUrl.isBlank()) {
            String springUrlEnv = System.getenv("SPRING_DATASOURCE_URL");
            if (springUrlEnv != null && springUrlEnv.startsWith("mysql://")) {
                mysqlUrl = springUrlEnv;
            }
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
                    System.err.println("⚠️ Failed to parse database URL: " + e.getMessage());
                }
            }
        }

        // Check if spring.datasource.url is still not set or is corrupted (e.g. jdbc:mysql://:/)
        String currentUrl = System.getProperty("spring.datasource.url");
        String envSpringUrl = System.getenv("SPRING_DATASOURCE_URL");

        boolean isCorruptedOrEmpty = (currentUrl == null || currentUrl.isBlank() || currentUrl.contains("://:/"))
                && (envSpringUrl == null || envSpringUrl.isBlank() || envSpringUrl.contains("://:/") || envSpringUrl.contains("localhost"));

        boolean isRailway = System.getenv("RAILWAY_ENVIRONMENT") != null
                || System.getenv("RAILWAY_SERVICE_ID") != null
                || System.getenv("RAILWAY_PROJECT_ID") != null;

        if (isRailway && isCorruptedOrEmpty) {
            System.out.println(">>> Railway environment detected with missing/corrupted DB URL. Applying Railway MySQL default fallback...");
            String defaultJdbc = "jdbc:mysql://mysql.railway.internal:3306/railway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            System.setProperty("spring.datasource.url", defaultJdbc);
            System.setProperty("spring.datasource.username", "root");
            System.setProperty("spring.datasource.password", "rjWyAsHMkZDjeUSgZaAfiHANTbYQiuDI");
            System.out.println(">>> Fallback configured: " + defaultJdbc);
        }

        System.out.println(">>> Effective spring.datasource.url: " + (System.getProperty("spring.datasource.url") != null ? System.getProperty("spring.datasource.url") : System.getenv("SPRING_DATASOURCE_URL")));
        System.out.println(">>> Effective spring.datasource.username: " + (System.getProperty("spring.datasource.username") != null ? System.getProperty("spring.datasource.username") : System.getenv("SPRING_DATASOURCE_USERNAME")));
        System.out.println("==================================================================");
    }
}
