package com.surevine.profileserver.db.jdbc.dialect;

import com.surevine.profileserver.db.jdbc.JDBCDataStore.DataStoreSQLDialect;

public class Sql92DataStoreDialect implements DataStoreSQLDialect {

	private static final String GET_OWNER = "SELECT * FROM owners WHERE \"owner\" = ?;";
	
	private static final String DELETE_OWNER = "DELETE FROM owners where \"owner\" = ?;";

	private static final String ADD_OWNER = "INSERT INTO owners VALUES(?, NOW());";
	
	@Override
	public String selectOwner() {
		return GET_OWNER;
	}
	
	@Override
	public String deleteOwner() {
		return DELETE_OWNER;
	}
	
	@Override
	public String addOwner() {
		return ADD_OWNER;
	}
}