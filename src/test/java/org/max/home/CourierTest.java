package org.max.home;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.max.seminar.CurrentEntity;

import javax.persistence.PersistenceException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourierTest extends AbstractTest{

    @Test
    @Order(1)
    void getCourier_whenValid_shouldReturn() throws SQLException {
        //given
        String sql = "SELECT * FROM courier_info WHERE delivery_type='car'";
        Statement stmt  = getConnection().createStatement();
        int countTableSize = 0;
        //when
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            countTableSize++;
        }
        final Query query = getSession().createSQLQuery("SELECT * FROM courier_info").addEntity(CourierInfoEntity.class);
        //then
        Assertions.assertEquals(3, countTableSize);
        Assertions.assertEquals(4, query.list().size());
    }

    @Order(2)
    @ParameterizedTest
    @CsvSource({"John, Rython", "Kate, Looran"})
    void getCourierById_whenValid_shouldReturn(String name, String lastName) throws SQLException {
        //given
        String sql = "SELECT * FROM courier_info WHERE first_name='" + name + "'";
        Statement stmt  = getConnection().createStatement();
        String nameString = "";
        //when
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            //для получения фамилии нужно использовать индекс 3
            nameString = rs.getString(2);
        }
        //then
        Assertions.assertEquals(lastName, nameString);
    }

    @Test
    @Order(3)
    void addCourier_whenValid_shouldSave() {
        //given
        CourierInfoEntity entity = new CourierInfoEntity();
        entity.setCourierId((short) 5);
        entity.setFirstName("Тамара");
        entity.setLastName("Новицкая");
        entity.setPhoneNumber("+ 7 981 162 4957");
        entity.setDeliveryType("велосипед");

        //when
        Session session = getSession();
        session.beginTransaction();
        session.persist(entity);
        session.getTransaction().commit();

        final Query query = getSession()
                .createSQLQuery("SELECT * FROM courier_info WHERE courier_id="+5).addEntity(CourierInfoEntity.class);
        CourierInfoEntity creditEntity = (CourierInfoEntity) query.uniqueResult();
        //then
        Assertions.assertNotNull(creditEntity);
        Assertions.assertEquals("велосипед", creditEntity.getDeliveryType());
    }

    @Test
    @Order(4)
    void deleteCourier_whenValid_shouldDelete() {
        //given
        final Query query = getSession()
                .createSQLQuery("SELECT * FROM courier_info WHERE courier_id=" + 5).addEntity(CourierInfoEntity.class);
        Optional<CourierInfoEntity> courierInfoEntity = (Optional<CourierInfoEntity>) query.uniqueResultOptional();
        Assumptions.assumeTrue(courierInfoEntity.isPresent());
        //when
        Session session = getSession();
        session.beginTransaction();
        session.delete(courierInfoEntity.get());
        session.getTransaction().commit();
        //then
        final Query queryAfterDelete = getSession()
                .createSQLQuery("SELECT * FROM courier_info WHERE courier_id=" + 5).addEntity(CourierInfoEntity.class);
        Optional<CourierInfoEntity> courierInfoEntityAfterDelete = (Optional<CourierInfoEntity>) queryAfterDelete.uniqueResultOptional();
        Assertions.assertFalse(courierInfoEntityAfterDelete.isPresent());
    }


    @Test
    @Order(5)
    void addCourier_whenNotValid_shouldThrow() {
        //given
        CourierInfoEntity entity = new CourierInfoEntity();
        //when
        Session session = getSession();
        session.beginTransaction();
        session.persist(entity);
        //then
        Assertions.assertThrows(PersistenceException.class, () -> session.getTransaction().commit());
        ;
    }

}
