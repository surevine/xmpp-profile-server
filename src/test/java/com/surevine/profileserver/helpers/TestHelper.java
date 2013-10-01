package com.surevine.profileserver.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import org.xmpp.packet.Packet;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.db.NodeStoreFactory;
import com.surevine.profileserver.db.NodeStoreFactoryImpl;
import com.surevine.profileserver.db.exception.NodeStoreException;
import com.surevine.profileserver.db.jdbc.DatabaseTester;
import com.surevine.profileserver.db.jdbc.JDBCNodeStore;
import com.surevine.profileserver.db.jdbc.dialect.Sql92NodeStoreDialect;
import com.surevine.profileserver.queue.InQueueConsumer;

public class TestHelper {
	LinkedBlockingQueue<Packet> outQueue;
	LinkedBlockingQueue<Packet> inQueue;
	InQueueConsumer consumer;
	
	NodeStoreFactory nodeStoreFactory;
	
	public TestHelper() throws FileNotFoundException, IOException {
		initialiseNodeStoreFactory();

        outQueue = new LinkedBlockingQueue<Packet>();
        inQueue = new LinkedBlockingQueue<Packet>();
        consumer = new InQueueConsumer(outQueue, Configuration.getInstance(), inQueue, nodeStoreFactory);
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
	  
	public NodeStoreFactory getChannelManagerFactory() {
		return nodeStoreFactory;
	}
	
    private NodeStoreFactory initialiseNodeStoreFactory() {
    	NodeStoreFactory nsFactory = new NodeStoreFactory() {
			
			@Override
			public NodeStore create() {
					try {
						return new JDBCNodeStore(new DatabaseTester().getConnection(), new Sql92NodeStoreDialect());
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
			nodeStoreFactory = new NodeStoreFactoryImpl(IQTestHandler.readConf());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NodeStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
	
}