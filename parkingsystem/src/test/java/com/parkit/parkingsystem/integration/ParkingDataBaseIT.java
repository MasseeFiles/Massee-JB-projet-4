package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.FareCalculatorService;
import java.util.Date;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();  //  objet pour vider BDD et remettre tt emplacements à libre avant chaque test - before each 
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //  WHEN
        parkingService.processIncomingVehicle();

        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability

        Ticket ticketSaved = (ticketDAO.getTicket("ABCDEF"));

        assertTrue(ticketSaved != null);    //  ticket = null au début de ticketDAO.getTicket()

        ParkingSpot parkingSpotSaved = ticketSaved.getParkingSpot();
        Boolean actualAvailabilityParkingSpotSaved = parkingSpotSaved.isAvailable();
        Boolean expectedAvailabilityParkingSpotSaved = false;   //valeur attendue à false car l'emplacement sauvé n'est plus libre

        assertEquals(expectedAvailabilityParkingSpotSaved , actualAvailabilityParkingSpotSaved);    
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();  //execution du test précedent dans ce test - quelles conséquences?
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processExitingVehicle();
        
        //TODO: check that the fare generated and out time are populated correctly in the database
//Fare generated
        Ticket ticketSaved = (ticketDAO.getTicket("ABCDEF"));
        assertTrue ((ticketDAO.updateTicket(ticketSaved)) == true); // sauvegarde de price + outTime réussi sur BDD 
        /*
        Double savedPrice = ticketSaved.getPrice();
        assertTrue(savedPrice ????);*/

//OutTime - necessaire???
/*
        Date savedOutTime = ticketSaved.getOutTime();
        Date expectedOutTime = new Date();*/
        long savedOutTimeMilli = ticketSaved.getOutTime().getTime();
        long expectedOutTimeMilli = new Date().getTime();
        assertTrue((savedOutTimeMilli - expectedOutTimeMilli) < 500 );    // valeurs separées de - de 0.5 seconde (pb au test sur format de la date)
    }

}
