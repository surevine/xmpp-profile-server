package com.surevine.profileserver.db.jdbc.dialect;

import com.surevine.profileserver.db.jdbc.JDBCNodeStore.NodeStoreSQLDialect;

public class Sql92DataStoreDialect implements NodeStoreSQLDialect {

	private static final String GET_OWNER = "SELECT * FROM owners WHERE \"owner\" = ?;";
	
	private static final String DELETE_OWNER = "DELETE FROM owners where \"owner\" = ?;";
	
	@Override
	public String selectOwner() {
		return GET_OWNER;
	}
	
	@Override
	public String deleteOwner() {
		return DELETE_OWNER;
	}
}