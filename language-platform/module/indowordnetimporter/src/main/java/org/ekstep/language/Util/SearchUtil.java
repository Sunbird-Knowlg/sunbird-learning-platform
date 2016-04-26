package org.ekstep.language.Util;

import java.util.Iterator;
import java.util.List;

import org.ekstep.language.model.EnglishSynsetDataLite;
import org.ekstep.language.model.KannadaSynsetDataLite;
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
	         query.setMaxResults(100);
	         List employees = query.list(); 
	         for (Iterator iterator = 
	                           employees.iterator(); iterator.hasNext();){/*
	        	TamilSynsetData employee = (TamilSynsetData) iterator.next(); 
	        	byte[] bSynset = employee.getSynset();
	        	String synsetString = new String(bSynset);
	            System.out.println("Word: " + synsetString);
	            for(TamilSynsetDataLite hypernym: employee.getHypernyms()){
	            	bSynset = hypernym.getSynset();
		        	String hsynsetString = new String(bSynset);
		            System.out.println("Hypernym: " + hsynsetString);
	            }
	            for(TamilSynsetDataLite hyponym: employee.getHyponyms()){
	            	bSynset = hyponym.getSynset();
		        	String hysynsetString = new String(bSynset);
		            System.out.println("Hyponym: " + hysynsetString);
	            }
	            for(TamilSynsetDataLite holonym: employee.getHolonyms()){
	            	bSynset = holonym.getSynset();
		        	String hysynsetString = new String(bSynset);
		            System.out.println("Holonym: " + hysynsetString);
	            }
	            for(TamilSynsetDataLite meronym: employee.getMeronyms()){
	            	bSynset = meronym.getSynset();
		        	String hysynsetString = new String(bSynset);
		            System.out.println("Meronym: " + hysynsetString);
	            }
	            for(TamilSynsetDataLite antonym: employee.getAntonyms()){
	            	bSynset = antonym.getSynset();
		        	String hysynsetString = new String(bSynset);
		            System.out.println("Antonym: " + hysynsetString);
	            }
	            for(TamilSynsetDataLite actionObject: employee.getActionObjects()){
	            	bSynset = actionObject.getSynset();
		        	String hysynsetString = new String(bSynset);
		            System.out.println("Action Object: " + hysynsetString);
	            }
	            
	            KannadaSynsetDataLite kannadaTranslation = employee.getKannadaTranslation();
	            bSynset = kannadaTranslation.getSynset();
	        	String hysynsetString = new String(bSynset);
	            System.out.println("Kannada Translation: " + hysynsetString);
	            
	            EnglishSynsetDataLite englishranslation = employee.getEnglishTranslation();
	            if(englishranslation != null){
		            bSynset = englishranslation.getSynset();
		        	hysynsetString = new String(bSynset);
		            System.out.println("English Translation: " + hysynsetString);
	            }
	         */}
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
