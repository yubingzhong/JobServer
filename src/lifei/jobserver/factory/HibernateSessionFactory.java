package lifei.jobserver.factory;

import java.io.File;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactory {
	
	private static SessionFactory sessionFactory = null;

	private HibernateSessionFactory() {
	}
	
	public static void build(File f) {
		if (sessionFactory == null)
			sessionFactory = new Configuration().configure(f)
					.buildSessionFactory();
	}
	
	public static Session getSession() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		return session;		
	}

}
