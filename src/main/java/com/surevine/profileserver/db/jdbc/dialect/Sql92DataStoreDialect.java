package com.surevine.profileserver.db.jdbc.dialect;

import com.surevine.profileserver.db.jdbc.JDBCNodeStore.NodeStoreSQLDialect;

public class Sql92DataStoreDialect implements NodeStoreSQLDialect {

	private static final String GET_OWNER = "SELECT * FROM owners WHERE \"owner\" = ?;";
	
	@Override
	public String selectOwner() {
		return GET_OWNER;
	}
}