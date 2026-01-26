package itmo.ivank.web.util;

import itmo.ivank.ejb.service.EmployeeServiceRemote;
import itmo.ivank.ejb.service.OrganizationServiceRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EjbLookup {

    private static final Logger logger = Logger.getLogger(EjbLookup.class.getName());

    private static volatile OrganizationServiceRemote organizationService;
    private static volatile EmployeeServiceRemote employeeService;
    private static volatile ConsulServiceDiscovery.ServiceEndpoint cachedEndpoint;

    private static ConsulServiceDiscovery.ServiceEndpoint getEjbEndpoint() {
        if (cachedEndpoint == null) {
            synchronized (EjbLookup.class) {
                if (cachedEndpoint == null) {
                    cachedEndpoint = ConsulServiceDiscovery.discoverEjbService();
                    logger.info("Discovered EJB service at: " + cachedEndpoint);
                }
            }
        }
        return cachedEndpoint;
    }

    private static Context createContext() throws NamingException {
        ConsulServiceDiscovery.ServiceEndpoint endpoint = getEjbEndpoint();
        
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, "remote+http://" + endpoint.getHost() + ":" + endpoint.getPort());
        // Disable authentication - anonymous access
        props.put("jboss.naming.client.ejb.context", "true");
        return new InitialContext(props);
    }

    public static OrganizationServiceRemote getOrganizationService() {
        if (organizationService == null) {
            synchronized (EjbLookup.class) {
                if (organizationService == null) {
                    try {
                        Context ctx = createContext();
                        String jndiName = "ejb:/called-ejb/OrganizationServiceBean!itmo.ivank.ejb.service.OrganizationServiceRemote";
                        logger.info("Looking up EJB: " + jndiName);
                        organizationService = (OrganizationServiceRemote) ctx.lookup(jndiName);
                        logger.info("Successfully obtained OrganizationServiceRemote");
                    } catch (NamingException e) {
                        logger.log(Level.SEVERE, "Failed to lookup OrganizationServiceRemote", e);
                        throw new RuntimeException("Failed to lookup OrganizationServiceRemote: " + e.getMessage(), e);
                    }
                }
            }
        }
        return organizationService;
    }

    public static EmployeeServiceRemote getEmployeeService() {
        if (employeeService == null) {
            synchronized (EjbLookup.class) {
                if (employeeService == null) {
                    try {
                        Context ctx = createContext();
                        String jndiName = "ejb:/called-ejb/EmployeeServiceBean!itmo.ivank.ejb.service.EmployeeServiceRemote";
                        logger.info("Looking up EJB: " + jndiName);
                        employeeService = (EmployeeServiceRemote) ctx.lookup(jndiName);
                        logger.info("Successfully obtained EmployeeServiceRemote");
                    } catch (NamingException e) {
                        logger.log(Level.SEVERE, "Failed to lookup EmployeeServiceRemote", e);
                        throw new RuntimeException("Failed to lookup EmployeeServiceRemote: " + e.getMessage(), e);
                    }
                }
            }
        }
        return employeeService;
    }
}
