package com.jpmorgan.cakeshop.test;

import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.bean.GethRunner;
import com.jpmorgan.cakeshop.config.AppStartup;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.service.task.BlockchainInitializerTask;
import com.jpmorgan.cakeshop.test.config.TempFileManager;
import com.jpmorgan.cakeshop.test.config.TestAppConfig;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.ProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@ActiveProfiles("test")
@ContextConfiguration(classes = {TestAppConfig.class})
//@Listeners(CleanConsoleListener.class) // uncomment for extra debug help
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class BaseGethRpcTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(BaseGethRpcTest.class);

    static {
        System.setProperty("spring.profiles.active", "test");
        System.setProperty("cakeshop.database.vendor", "hsqldb");
    }

    @Autowired
    private ContractService contractService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AppStartup appStartup;

    @Autowired
    protected GethHttpService geth;

    @Value("${geth.datadir}")
    private String ethDataDir;

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Autowired
    private GethRunner gethRunner;

    @Autowired
    private GethConfig gethConfig;

    @Autowired
    @Qualifier("hsql")
    private DataSource embeddedDb;

    public BaseGethRpcTest() {
        super();
    }

    public boolean runGeth() {
        return true;
    }

    @AfterSuite(alwaysRun = true)
    public void stopSolc() throws IOException {
        List<String> args = Lists.newArrayList(
                gethConfig.getNodeJsBinaryName(),
                gethRunner.getSolcPath(),
                "--stop-ipc");

        ProcessBuilder builder = ProcessUtils.createProcessBuilder(gethRunner, args);
        builder.start();
    }

    @AfterSuite(alwaysRun = true)
    public void cleanupTempPaths() {
        try {
            if (CONFIG_ROOT != null) {
                FileUtils.deleteDirectory(new File(CONFIG_ROOT));
            }
            TempFileManager.cleanupTempPaths();
        } catch (IOException e) {
        }
    }

    @BeforeClass
    public void startGeth() throws IOException {
        if (!runGeth()) {
            return;
        }

        assertTrue(appStartup.isHealthy(), "Healthcheck should pass");
        LOG.info("Starting Ethereum at test startup");
        assertTrue(_startGeth());
        initializeChain();
    }

    private boolean _startGeth() throws IOException {
        gethRunner.setGenesisBlockFilename(FileUtils.getClasspathPath("genesis_block.json").toAbsolutePath().toString());
        gethRunner.setKeystorePath(FileUtils.getClasspathPath("keystore").toAbsolutePath().toString());
        return geth.start();
    }

    /**
     * Stop geth & delete data dir
     */
    @AfterClass(alwaysRun = true)
    public void stopGeth() {
        if (!runGeth()) {
            return;
        }
        LOG.info("Stopping Ethereum at test teardown");
        _stopGeth();
    }

    private void _stopGeth() {
        geth.stop();
        try {
            FileUtils.deleteDirectory(new File(ethDataDir));
        } catch (IOException e) {
            logger.warn(e);
        }
        String db = System.getProperty("cakeshop.database.vendor");
        if (db.equalsIgnoreCase("hsqldb")) {
            ((EmbeddedDatabase) embeddedDb).shutdown();
        }
    }

    /**
     * Read the given test resource file
     *
     * @param path
     * @return
     * @throws IOException
     */
    protected String readTestFile(String path) throws IOException {
        return FileUtils.readClasspathFile(path);
    }

    /**
     * Deploy SimpleStorage sample to the chain and return its address
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    protected String createContract() throws IOException, InterruptedException {
        String code = readTestFile("contracts/simplestorage.sol");
        return createContract(code, new Object[]{100}, "simplestorage.sol");
    }

    /**
     * Deploy the given contract code to the chain
     *
     * @param code
     * @param filename
     * @return
     * @throws APIException
     * @throws InterruptedException
     */
    protected String createContract(String code, Object[] args, String filename) throws APIException, InterruptedException {
        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, args, null, null, null,
            filename, true, "byzantium");
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(!result.getId().isEmpty());

        if (gethConfig.getConsensusMode().equals("istanbul")) {
            Map<String, Object> res = geth.executeGethCall("miner_start", new Object[]{});
        }

        Transaction tx = transactionService.waitForTx(result, 50, TimeUnit.MILLISECONDS);
        return tx.getContractAddress();
    }


    void initializeChain() throws APIException {
        BlockchainInitializerTask chainInitTask =
            applicationContext.getBean(BlockchainInitializerTask.class);
        chainInitTask.run(); // run in same thread
    }
}
