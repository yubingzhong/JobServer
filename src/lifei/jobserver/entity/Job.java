package lifei.jobserver.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "jobs")
public class Job {

	private static final long serialVersionUID = 7832959970645721837L;

	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	public long id;

	public String user;
	public String command;
	public String name;
	public String desc;
	public long createtime;
	public long executetime;
	public long finishtime;
	public long exitvalue;
	
	@Column(columnDefinition="text")
	public String stdout;
	@Column(columnDefinition="text")
	public String stderr;
	public String workdir;
	public int status;
	public String error = "";

	public String type = "";

	@SuppressWarnings("unused")
	private Job() {
		this.createtime = (System.currentTimeMillis());
		this.executetime = 0;
		this.finishtime = 0;
		this.status = 0;
		this.stderr = "";
		this.stdout = "";
		this.workdir = "";
		this.user = "";
		this.name = "";
		this.desc = "";
		this.command = "";
	}

	public Job(String user, String command, String name, String type, String desc) {
		this.createtime = System.currentTimeMillis();
		this.executetime = 0;
		this.finishtime = 0;
		this.status = 0;
		this.stderr = "";
		this.stdout = "";
		this.workdir = "";
		this.user = user;
		this.name = name;
		this.desc = desc;
		this.command = command;
		this.type = type;
	}
	
	public boolean save(Session session) {
		
		try {
			if (session.getTransaction() == null) {
				session.beginTransaction();
			} else {
				if(!session.getTransaction().isActive()) {
					session.getTransaction().begin();
				}
			}

			session.saveOrUpdate(this);
		} catch (Exception e) {
			return false;
		} finally {
			session.getTransaction().commit();
		}

		return true;
	}
}
