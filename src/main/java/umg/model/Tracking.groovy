package umg.model

import umg.Db


public class Tracking extends Db {
    private static String RESOURCE_ID = "resource_id"

    private long id;
    private String resouce_id;
    private String status;

    Tracking() {
        if (this.sql == null) {
            this.init()
        }
    }

    Tracking(String resouce_id, String status) {
        this.resouce_id = resouce_id
        this.status = status
    }

    /**
     *
     */
    public void createTackingTable() {
        dropTable()
        try {
            sql.execute '''
     CREATE TABLE TRACKING (
         id integer not null PRIMARY KEY,
         resource_id varchar(50),
         status varchar(50),
         notes varchar(50)
     )
 '''
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    /**
     *
     */
    public void dropTable() {
        try {
            sql.execute("DROP TABLE TRACKING");
        } catch (Exception e) {
        }
    }

    public boolean insert(int id, String resourceId) {
        try {
            boolean result = sql.execute("INSERT INTO TRACKING (id, resource_id) VALUES ( ?, ? )", [id, resourceId])
            sql.commit()
            println "insert result " + result
            return result
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
    }

    /**
     *  upsert
     */
    public boolean upsert(int id, String resourceId) {
        try {
            boolean result = sql.execute("UPDATE TRACKING SET resource_id = ? where id = ?", [ resourceId, id])
            sql.commit()
            println "update result " + result
            return result
        } catch (Exception e) {
            try {
                println "update failded doing insert "
                boolean result = sql.execute("INSERT INTO TRACKING (id, resource_id) VALUES ( ?, ? )", [id, resourceId])
                sql.commit()
                println "insert result " + result
                return result
            } catch (Exception f) {
                return false
            }
        }
    }

    /**
     *
     */
    public void selectId(int id) {
        try {
            def rows = sql.rows("select * from TRACKING where id = ?", [id])
            rows.each { row ->
                println "${row.id} \t ${row.resource_id}"
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    public static void main(String[] args) {
        println "running tracking .. "
        Tracking t = new Tracking()
//        t.createTackingTable()
//        for (int i = 3; i < 100000; i++) {
//            t.insert( i , "resouceId_12345")
//        }
        t.upsert(400, "new value 2")
        t.selectId(400)

    }
}
