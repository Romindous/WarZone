package me.Romindous.WarZone.SQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import me.Romindous.WarZone.Main;
import ru.komiss77.ApiOstrov;

public class SQLGet {
	
	public void mkTbl(final String tbl, final String... cts) {
		try {
			Bukkit.getLogger().info("CREATE TABLE IF NOT EXISTS " + tbl + "(" + getVars(cts) + "PRIMARY KEY (" + cts[0].toUpperCase() + "))");
			exctStrStmt("CREATE TABLE IF NOT EXISTS " + tbl + "(" + getVars(cts) + "PRIMARY KEY (" + cts[0].toUpperCase() + "))").executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String getVars(final String[] cts) {
		final StringBuffer sb = new StringBuffer("");
		for (final String s : cts) {
			sb.append(s.toUpperCase() + " VARCHAR(20),");
		}
		return sb.toString();
	}
	
	public void setString(final String name, final String cat, final String set) {
		try {
			exctStrStmt("UPDATE " + Main.tbl + " SET " + cat.toUpperCase() + "=? WHERE NAME=?", set, name).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getString(final String name, final String cat) {
		try {
			final ResultSet rs = exctStrStmt("SELECT * FROM " + Main.tbl + " WHERE NAME=?", name).executeQuery();
			return rs.next() ? rs.getString(cat.toUpperCase()) : null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void chngNum(final String name, final String cat, final int n) {
		try {
			final ResultSet rs = exctStrStmt("SELECT * FROM " + Main.tbl + " WHERE NAME=?", name).executeQuery(); rs.next();
			final PreparedStatement ps = ApiOstrov.getLocalConnection().prepareStatement("UPDATE " + Main.tbl + " SET " + cat.toUpperCase() + "=? WHERE NAME=?");
			ps.setInt(1, rs.getInt(cat.toUpperCase()) + n);
			ps.setString(2, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//deletion
	public void delTbl(final String tbl) {
		try {
			exctStrStmt("DROP TABLE " + tbl).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public PreparedStatement exctStrStmt(final String comm, final String... vars) throws SQLException {
		final PreparedStatement ps = ApiOstrov.getLocalConnection().prepareStatement(comm);
		for (byte i = 1; i <= vars.length; i++) {
			ps.setString(i, vars[i-1]);
		}
		return ps;
	}

	public void chckIfExsts(final String name) {
		try {
			final ResultSet rs = exctStrStmt("SELECT * FROM " + Main.tbl + " WHERE NAME=?", name).executeQuery();
			if (!rs.next()) {
				final PreparedStatement ps = ApiOstrov.getLocalConnection().prepareStatement("INSERT IGNORE INTO " + Main.tbl + "(NAME,KLS,DTHS,RSPS,WNS,LSS,PRM) VALUES (?,?,?,?,?,?,?)");
				ps.setString(1, name);
				ps.setInt(2, 0);
				ps.setInt(3, 0);
				ps.setInt(4, 0);
				ps.setInt(5, 0);
				ps.setInt(6, 0);
				ps.setString(7, "N");
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
