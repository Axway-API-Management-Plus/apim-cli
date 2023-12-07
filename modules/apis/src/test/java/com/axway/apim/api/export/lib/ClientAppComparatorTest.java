package com.axway.apim.api.export.lib;

import com.axway.apim.api.model.apps.ClientApplication;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ClientAppComparatorTest {

    @Test
    public void sortEmptyClientApplications() {
        List<ClientApplication> clientApplicationList = new ArrayList<>();
        clientApplicationList.sort(new ClientAppComparator());
        Assert.assertTrue(clientApplicationList.isEmpty());
    }

    @Test
    public void sortClientApplicationsWithoutName() {
        List<ClientApplication> clientApplicationList = new ArrayList<>();
        ClientApplication clientApplication = new ClientApplication();
        clientApplicationList.add(clientApplication);
        ClientApplication clientApplication2 = new ClientApplication();
        clientApplicationList.add(clientApplication2);
        clientApplicationList.sort(new ClientAppComparator());
        Assert.assertEquals(clientApplicationList.size(), 2);
    }


    @Test
    public void sortClientApplicationsWithName() {
        List<ClientApplication> clientApplicationList = new ArrayList<>();
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName("xyz");
        clientApplicationList.add(clientApplication);
        ClientApplication clientApplication2 = new ClientApplication();
        clientApplication2.setName("abc");

        clientApplicationList.add(clientApplication2);
        clientApplicationList.sort(new ClientAppComparator());
        Assert.assertEquals(clientApplicationList.get(0).getName(), "abc");
        Assert.assertEquals(clientApplicationList.get(1).getName(), "xyz");

    }

    @Test
    public void sortClientApplicationsWithNameOne() {
        List<ClientApplication> clientApplicationList = new ArrayList<>();
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName("xyz");
        clientApplicationList.add(clientApplication);
        clientApplicationList.sort(new ClientAppComparator());
        Assert.assertEquals(clientApplicationList.get(0).getName(), "xyz");

    }

    @Test
    public void compareApp1Empty() {
        ClientAppComparator clientAppComparator = new ClientAppComparator();
        Assert.assertEquals(clientAppComparator.compare(null, new ClientApplication()), 0);
    }

    @Test
    public void compareApp2Empty() {
        ClientAppComparator clientAppComparator = new ClientAppComparator();
        Assert.assertEquals(clientAppComparator.compare(new ClientApplication(), null), 0);
    }

    @Test
    public void compareApp1EmptyName() {
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName("abc");
        ClientAppComparator clientAppComparator = new ClientAppComparator();
        Assert.assertEquals(clientAppComparator.compare(null, clientApplication), 0);
    }

    @Test
    public void compareApp2EmptyName() {
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName("abc");
        ClientAppComparator clientAppComparator = new ClientAppComparator();
        Assert.assertEquals(clientAppComparator.compare(clientApplication, null), 0);
    }


}
