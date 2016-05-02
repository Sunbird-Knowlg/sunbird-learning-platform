package org.ekstep.language.Util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

@SuppressWarnings("deprecation")
public class HibernateSessionFactory {

	private static SessionFactory sessionFactory;
	private static Session session;

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
		if (session == null || !session.isOpen()) {
			if (sessionFactory == null) {
				buildSessionFactory();
			}
			session = (sessionFactory != null) ? sessionFactory.openSession() : null;
		}
		return session;
	}

	public static void buildSessionFactory() {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void closeSession() throws HibernateException {
		if (session != null) {
			session.flush();
			session.close();
		}
	}

	public static org.hibernate.SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}