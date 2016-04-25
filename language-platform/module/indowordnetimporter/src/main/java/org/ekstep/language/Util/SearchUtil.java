package org.ekstep.language.Util;

import java.util.Iterator;
import java.util.List;

import org.ekstep.language.model.TamilSynsetData;
import org.ekstep.language.model.TamilSynsetDataLite;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

@SuppressWarnings("deprecation")
public class SearchUtil {

	private static final SessionFactory sessionFactory = buildSessionFactory();
	@SuppressWarnings("rawtypes")
	public void listEmployees( ){
	      Session session = sessionFactory.openSession();
	      Transaction tx = null;
	      try{
	         tx = session.beginTransaction();
	         Query query = session.createQuery("FROM TamilSynsetData");
	         //query.setFirstResult(10);
	         query.setMaxResults(10);
	         List employees = query.list(); 
	         for (Iterator iterator = 
	                           employees.iterator(); iterator.hasNext();){
	        	TamilSynsetData employee = (TamilSynsetData) iterator.next(); 
	        	byte[] bSynset = employee.getSynset();
	        	String synsetString = new String(bSynset);
	            System.out.println("First Name: " + synsetString);
	            for(TamilSynsetDataLite hypernym: employee.getHypernyms()){
	            	byte[] bHSynset = hypernym.getSynset();
		        	String hsynsetString = new String(bHSynset);
		            System.out.println("Hypernym First Name: " + hsynsetString);
	            }
	            for(TamilSynsetDataLite hyponym: employee.getHyponyms()){
	            	byte[] bHySynset = hyponym.getSynset();
		        	String hysynsetString = new String(bHySynset);
		            System.out.println("Hyponym First Name: " + hysynsetString);
	            }
	         }
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	         session.close(); 
	      }
	   }
	
	
	

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            return new AnnotationConfiguration().configure().buildSessionFactory();
            
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    public static void shutdown() {
    	// Close caches and connection pools
    	getSessionFactory().close();
    }

	
	public static void main(String[] args) {
		SearchUtil util = new SearchUtil();
		util.listEmployees();
	}

}
