package org.opencube.junit5.dbprovider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.sql.DataSource;

import com.github.dockerjava.api.model.PortBinding;
import com.mysql.cj.jdbc.MysqlDataSource;

import aQute.bnd.annotation.spi.ServiceProvider;

@ServiceProvider(value = DatabaseProvider.class)
public class MySqlDatabaseProvider extends AbstractDockerBasesDatabaseProvider {

	public static String MYSQL_ROOT_PASSWORD = "the.root.pw";
	public static String MYSQL_DATABASE = "the.db";
	public static String MYSQL_USER = "the.user";
	public static String MYSQL_PASSWORD = "the.pw";
	public static int PORT = 3306;
	public static String serverName = "0.0.0.0";

	@Override
	protected PortBinding portBinding() {
		return PortBinding.parse(PORT + ":" + PORT);
	}

	@Override
	protected List<String> env() {
		ArrayList<String> envs = new ArrayList<>();
		envs.add("MYSQL_ROOT_PASSWORD=" + MYSQL_ROOT_PASSWORD);
		envs.add("MYSQL_USER=" + MYSQL_USER);
		envs.add("MYSQL_PASSWORD=" + MYSQL_PASSWORD);
		envs.add("MYSQL_DATABASE=" + MYSQL_DATABASE);

		return envs;
	}

	@Override
	protected Entry<String, DataSource> createConnection() {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setServerName(serverName);
		dataSource.setPort(PORT);
		dataSource.setPassword(MYSQL_PASSWORD);
		dataSource.setUser(MYSQL_USER);
		dataSource.setDatabaseName(MYSQL_DATABASE);
		try {
			dataSource.setRewriteBatchedStatements(true);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < 1000; i++) {
			System.out.println(i);

			try {
				Thread.sleep(100);

				Connection connection = dataSource.getConnection(MYSQL_USER, MYSQL_PASSWORD);
				String connString = "jdbc:mysql://" + serverName + ":" + PORT + "/" + MYSQL_DATABASE + "?user="
						+ MYSQL_USER + "&password=" + MYSQL_PASSWORD;

				// jdbc:mysql://<hostname>:<port>/<dbname>?prop1;
				return new AbstractMap.SimpleEntry<String, DataSource>(connString, dataSource);

			} catch (Exception e) {
//				e.printStackTrace();
				System.out.println("nope");
			}

		}
		return null;
	}

	@Override
	protected String image() {
		return "mysql:latest";
	}

	@Override
	public String getJdbcUrl() {
		return "jdbc:mysql" + "//" + serverName + ":" + PORT + "/" + MYSQL_DATABASE + "?user=" + MYSQL_USER
				+ "&password=" + MYSQL_PASSWORD + "&rewriteBatchedStatements=true";
	}

}