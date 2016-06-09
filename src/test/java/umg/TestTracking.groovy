package umg

import org.apache.camel.test.junit4.CamelTestSupport
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import umg.model.Tracking

import javax.persistence.Persistence

class TestTracking extends CamelTestSupport {


    @Test
    public void crudTracking(){
        def factory = Persistence.createEntityManagerFactory("AddressBookStore")
        def manager = factory.createEntityManager()
        manager.getTransaction().begin()
        manager.persist new Tracking("resourceid_12345", "pending")
        manager.getTransaction().commit()

    }
}


