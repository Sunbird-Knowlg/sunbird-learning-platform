package org.ekstep.language.Util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import org.ekstep.common.dto.CoverageIgnore;
@CoverageIgnore
@SuppressWarnings("deprecation")
public class HibernateSessionFactory {

	private static SessionFactory sessionFactory;

	static {
		try {
			buildSessionFactory();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private HibernateSessionFactory() {
	}

	public static Session getSession() throws HibernateException {
		if (sessionFactory == null) {
			buildSessionFactory();
		}
		return sessionFactory.openSession();
	}

	public static void buildSessionFactory() {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static org.hibernate.SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}