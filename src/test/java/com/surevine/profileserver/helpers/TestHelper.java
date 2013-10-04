package com.surevine.profileserver.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import org.xmpp.packet.Packet;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.DataStoreFactory;
import com.surevine.profileserver.db.DataStoreFactoryImpl;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.db.jdbc.DatabaseTester;
import com.surevine.profileserver.db.jdbc.JDBCDataStore;
import com.surevine.profileserver.db.jdbc.dialect.Sql92DataStoreDialect;
import com.surevine.profileserver.queue.InQueueConsumer;

public class TestHelper {
	LinkedBlockingQueue<Packet> outQueue;
	LinkedBlockingQueue<Packet> inQueue;
	InQueueConsumer consumer;
	
	DataStoreFactory dataStoreFactory;
	
	public TestHelper() throws FileNotFoundException, IOException {
		initialiseDataStoreFactory();

        outQueue = new LinkedBlockingQueue<Packet>();
        inQueue = new LinkedBlockingQueue<Packet>();
        consumer = new InQueueConsumer(outQueue, Configuration.getInstance(), inQueue, dataStoreFactory);
        consumer.start();
	}
	
	public LinkedBlockingQueue<Packet> getOutQueue() {
		return outQueue;
	}
	
	public LinkedBlockingQueue<Packet> getInQueue() {
		return inQueue;
	}
	
	public InQueueConsumer getConsumer() {
		return consumer;
	}
	  
	public DataStoreFactory getChannelManagerFactory() {
		return dataStoreFactory;
	}
	
    private DataStoreFactory initialiseDataStoreFactory() {
    	DataStoreFactory nsFactory = new DataStoreFactory() {
			
			@Override
			public DataStore create() {
					try {
						return new JDBCDataStore(new DatabaseTester().getConnection(), new Sql92DataStoreDialect());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
			}
		};
    	
		try {
			dataStoreFactory = new DataStoreFactoryImpl(IQTestHandler.readConf());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
	
}