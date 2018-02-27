package fr.inria.anhalytics.commons.main;

import fr.inria.anhalytics.commons.utilities.ScriptRunner;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * @author azhar
 */
public class Main {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) throws IOException, InterruptedException, SQLException {

        String option = "";
        String currArg;
        for (int i = 0; i < args.length; i++) {
            currArg = args[i];
            if (currArg.equals("--configure")) {
                option = "configure";
                i++;
                continue;
            } else if (currArg.equals("--prepare")) {
                option = "prepare";
                i++;
                continue;
            } else {
                break;
            }
        }

        if (option.equals("configure")) {
            configure();
        } else if (option.equals("prepare")) {
            prepare();
        } else {
            System.out.println(getHelp());
        }
    }

    protected static void prepare() throws IOException, InterruptedException, SQLException {
        File file = new File(System.getProperty("user.dir"));
        Properties props = new Properties();
        FileInputStream in;
        Connection connectDB = null;
        Connection connectBiblioDB;
        try {
            in = new FileInputStream(file.getAbsolutePath() + File.separator + "config" + File.separator + "anhalytics.properties");

            props.load(in);
            in.close();
        } catch (IOException ex) {
            System.err.println(ANSI_RED + "You should set the configuration using --configure ." + ANSI_RESET);
            return;
        }
        System.out.println(ANSI_GREEN + "Schema creation in progress... " + ANSI_RESET);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connectDB = DriverManager.getConnection("jdbc:mysql://" + props.getProperty("kb.mysql_host")
                    + (props.getProperty("kb.mysql_port").isEmpty() ? "" : ":" + props.getProperty("kb.mysql_port")) + "?" + "user=" + props.getProperty("kb.mysql_user") + "&password=" + props.getProperty("kb.mysql_pass"));
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to get mysql driver: " + e);
        } catch (SQLException e) {
            System.err.println("Unable to connect to server: " + e);
        }

        ScriptRunner runner = new ScriptRunner(connectDB, false, false);

        String createSchema = "CREATE SCHEMA IF NOT EXISTS `" + props.getProperty("kb.mysql_db")
                + "` DEFAULT CHARACTER SET utf8;\n" +
                "USE `" + props.getProperty("kb.mysql_db") + "`;";
        runner.runScript(new BufferedReader(new StringReader(createSchema)));
        String sql1 = file.getAbsolutePath() + File.separator + "sql" + File.separator + "anhalyticsDB.sql";
        runner.runScript(new BufferedReader(new FileReader(sql1)));

        String createSchemaBiblio = "CREATE SCHEMA IF NOT EXISTS `" + props.getProperty("kb.mysql_bibliodb")
                + "` DEFAULT CHARACTER SET utf8;\n" +
                "USE `" + props.getProperty("kb.mysql_bibliodb") + "`;";
        runner.runScript(new BufferedReader(new StringReader(createSchemaBiblio)));
        String sql2 = file.getAbsolutePath() + File.separator + "sql" + File.separator + "biblioDB.sql";
        runner.runScript(new BufferedReader(new FileReader(sql2)));
    }

    protected static void configure() throws IOException {
        System.out.println(ANSI_GREEN + "Parameters settings " + ANSI_RESET);
        File file = new File(System.getProperty("user.dir"));

        FileInputStream in = new FileInputStream(file.getAbsolutePath() + File.separator + "config" + File.separator + "anhalytics.default.properties");
        Properties defaultprops = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<Object>(super.keySet()));
            }
        };
        defaultprops.load(in);
        in.close();

        // MONGO DB
        System.out.println(ANSI_BLUE + "Mongo DB" + ANSI_RESET);

        Scanner scanner = new Scanner(System.in);

        String mongodb_host = defaultprops.getProperty("commons.mongodb_host");
        System.out.print(ANSI_YELLOW + "mongodb_host (" + mongodb_host + "): " + ANSI_RESET);
        mongodb_host = scanner.nextLine();
        if (!mongodb_host.isEmpty()) {
            defaultprops.setProperty("commons.mongodb_host", mongodb_host);
        }

        int mongodb_port = Integer.parseInt(defaultprops.getProperty("commons.mongodb_port"));
        System.out.print(ANSI_YELLOW + "mongodb_port (" + mongodb_port + "): " + ANSI_RESET);
        try {
            mongodb_port = Integer.parseInt(scanner.nextLine());
            if (mongodb_port >= 65535) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect port number" + ANSI_RESET);
        }
        if (mongodb_port != 0) {
            defaultprops.setProperty("commons.mongodb_port", Integer.toString(mongodb_port));
        }

        String mongodb_name = defaultprops.getProperty("commons.mongodb_db");
        System.out.print(ANSI_YELLOW + "mongodb_name (" + mongodb_name + "): " + ANSI_RESET);
        mongodb_name = scanner.nextLine();
        if (!mongodb_name.isEmpty()) {
            defaultprops.setProperty("commons.mongodb_db", mongodb_name);
        }

        String mongodb_user = defaultprops.getProperty("commons.mongodb_user");
        System.out.print(ANSI_YELLOW + "mongodb_user (" + mongodb_user + "): " + ANSI_RESET);
        mongodb_user = scanner.nextLine();
        if (!mongodb_user.isEmpty()) {
            defaultprops.setProperty("commons.mongodb_user", mongodb_user);
        }

        String mongodb_pass = defaultprops.getProperty("commons.mongodb_pass");
        System.out.print(ANSI_YELLOW + "mongodb_pass (" + mongodb_pass + "): " + ANSI_RESET);
        mongodb_pass = scanner.nextLine();
        if (!mongodb_pass.isEmpty()) {
            defaultprops.setProperty("commons.mongodb_pass", mongodb_pass);
        }

        System.out.println();
        // GROBID
        System.out.println(ANSI_BLUE + "GROBID" + ANSI_RESET);
        String grobid_host = defaultprops.getProperty("harvest.grobid_host");
        System.out.print(ANSI_YELLOW + "grobid_host (" + grobid_host + "): " + ANSI_RESET);
        grobid_host = scanner.nextLine();
        if (!grobid_host.isEmpty()) {
            defaultprops.setProperty("harvest.grobid_host", grobid_host);
        }

        int grobid_port = Integer.parseInt(defaultprops.getProperty("harvest.grobid_port"));
        System.out.print(ANSI_YELLOW + "grobid_port (" + grobid_port + "): " + ANSI_RESET);
        try {
            grobid_port = Integer.parseInt(scanner.nextLine());
            if (grobid_port >= 65535) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect port number, retry :" + ANSI_RESET);
        }
        if (grobid_port != 0) {
            defaultprops.setProperty("harvest.grobid_port", Integer.toString(grobid_port));
        }

        int nb_threads = Integer.parseInt(defaultprops.getProperty("harvest.nbThreads"));
        System.out.print(ANSI_YELLOW + "nb_threads (" + nb_threads + "): " + ANSI_RESET);
        try {
            nb_threads = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect should be signed number, retry :" + ANSI_RESET);
        }
        if (nb_threads != 0) {
            defaultprops.setProperty("harvest.nbThreads", Integer.toString(nb_threads));
        }

        String tmp_path = defaultprops.getProperty("harvest.tmpPath");
        System.out.print(ANSI_YELLOW + "temporary directory (" + tmp_path + ") : " + ANSI_RESET);
        tmp_path = scanner.nextLine();
        if (!tmp_path.isEmpty()) {
            defaultprops.setProperty("harvest.tmpPath", tmp_path);
        }

        // NERD
        System.out.println(ANSI_BLUE + "NERD" + ANSI_RESET);
        String nerd_host = defaultprops.getProperty("annotate.nerd_host");
        System.out.print(ANSI_YELLOW + "nerd_host (" + nerd_host + "): " + ANSI_RESET);
        nerd_host = scanner.nextLine();
        if (!nerd_host.isEmpty()) {
            defaultprops.setProperty("annotate.nerd_host", nerd_host);
        }

        int nerd_port = Integer.parseInt(defaultprops.getProperty("annotate.nerd_port"));
        System.out.print(ANSI_YELLOW + "nerd_port (" + nerd_port + "): " + ANSI_RESET);
        try {
            nerd_port = Integer.parseInt(scanner.nextLine());
            if (nerd_port >= 65535) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect port number" + ANSI_RESET);
        }
        if (nerd_port != 0) {
            defaultprops.setProperty("annotate.nerd_port", Integer.toString(nerd_port));
        }

        int nb_threads_nerd = Integer.parseInt(defaultprops.getProperty("annotate.nerd.nbThreads"));
        System.out.print(ANSI_YELLOW + "nb_threads (" + nb_threads_nerd + "): " + ANSI_RESET);
        try {
            nb_threads_nerd = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect should be signed number" + ANSI_RESET);
        }
        if (nb_threads_nerd != 0) {
            defaultprops.setProperty("annotate.nerd.nbThreads", Integer.toString(nb_threads_nerd));
        }

        // KEYTERM
        System.out.println(ANSI_BLUE + "KEYTERM" + ANSI_RESET);
        String keyterm_host = defaultprops.getProperty("annotate.keyterm_host");
        System.out.print(ANSI_YELLOW + "keyterm_host (" + keyterm_host + "): " + ANSI_RESET);
        keyterm_host = scanner.nextLine();
        if (!keyterm_host.isEmpty()) {
            defaultprops.setProperty("annotate.keyterm_host", keyterm_host);
        }

        int keyterm_port = Integer.parseInt(defaultprops.getProperty("annotate.keyterm_port"));
        System.out.print(ANSI_YELLOW + "keyterm_port (" + keyterm_port + "): " + ANSI_RESET);
        try {
            keyterm_port = Integer.parseInt(scanner.nextLine());
            if (nerd_port >= 65535) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect port number" + ANSI_RESET);
        }
        if (keyterm_port != 0) {
            defaultprops.setProperty("annotate.keyterm_port", Integer.toString(keyterm_port));
        }

        int nb_threads_keyterm = Integer.parseInt(defaultprops.getProperty("annotate.keyterm.nbThreads"));
        System.out.print(ANSI_YELLOW + "nb_threads (" + nb_threads_keyterm + "): " + ANSI_RESET);
        try {
            nb_threads_keyterm = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect should be signed number" + ANSI_RESET);
        }
        if (nb_threads_keyterm != 0) {
            defaultprops.setProperty("annotate.keyterm.nbThreads", Integer.toString(nb_threads_keyterm));
        }

        // MYSQL
        System.out.println(ANSI_BLUE + "MYSQL" + ANSI_RESET);
        String mysql_url = defaultprops.getProperty("kb.mysql_host");
        System.out.print(ANSI_YELLOW + "mysql_host (" + mysql_url + "): " + ANSI_RESET);
        mysql_url = scanner.nextLine();
        if (!mysql_url.isEmpty()) {
            defaultprops.setProperty("kb.mysql_host", mysql_url);
        }

        int mysql_port = Integer.parseInt(defaultprops.getProperty("kb.mysql_port"));
        System.out.print(ANSI_YELLOW + "mysql_port (" + mysql_port + "): " + ANSI_RESET);
        try {
            mysql_port = Integer.parseInt(scanner.nextLine());
            if (mysql_port >= 65535) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect port number" + ANSI_RESET);
        }
        if (mysql_port != 0) {
            defaultprops.setProperty("kb.mysql_port", Integer.toString(mysql_port));
        }

        String mysql_dbname = defaultprops.getProperty("kb.mysql_db");
        System.out.print(ANSI_YELLOW + "mysql_dbname (" + mysql_dbname + "): " + ANSI_RESET);
        mysql_dbname = scanner.nextLine();
        if (!mysql_dbname.isEmpty()) {
            defaultprops.setProperty("kb.mysql_db", mysql_dbname);
        }

        String mysql_bibliodbname = defaultprops.getProperty("kb.mysql_bibliodb");
        System.out.print(ANSI_YELLOW + "mysql_bibliodbname (" + mysql_bibliodbname + "): " + ANSI_RESET);
        mysql_bibliodbname = scanner.nextLine();
        if (!mysql_bibliodbname.isEmpty()) {
            defaultprops.setProperty("kb.mysql_bibliodb", mysql_bibliodbname);
        }

        String mysql_user = defaultprops.getProperty("kb.mysql_user");
        System.out.print(ANSI_YELLOW + "mysql_user (" + mysql_user + "): " + ANSI_RESET);
        mysql_user = scanner.nextLine();
        if (!mysql_user.isEmpty()) {
            defaultprops.setProperty("kb.mysql_user", mysql_user);
        }

        String mysql_pass = defaultprops.getProperty("kb.mysql_pass");
        System.out.print(ANSI_YELLOW + "mysql_pass (" + mysql_pass + "): " + ANSI_RESET);
        mysql_pass = scanner.nextLine();
        if (!mysql_pass.isEmpty()) {
            defaultprops.setProperty("kb.mysql_pass", mysql_pass);
        }

        // ElasticSearch
        System.out.println(ANSI_BLUE + "ElasticSearch" + ANSI_RESET);
        String elasticsearch_host = defaultprops.getProperty("index.elasticSearch_host");
        System.out.print(ANSI_YELLOW + "elasticsearch_host (" + elasticsearch_host + "): " + ANSI_RESET);
        elasticsearch_host = scanner.nextLine();
        if (!elasticsearch_host.isEmpty()) {
            defaultprops.setProperty("index.elasticSearch_host", elasticsearch_host);
        }

        int elasticsearch_port = Integer.parseInt(defaultprops.getProperty("index.elasticSearch_port"));
        System.out.print(ANSI_YELLOW + "elasticsearch_port (" + elasticsearch_port + "): " + ANSI_RESET);
        try {
            elasticsearch_port = Integer.parseInt(scanner.nextLine());
            if (elasticsearch_port >= 65535) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "incorrect port number" + ANSI_RESET);
        }
        if (elasticsearch_port != 0) {
            defaultprops.setProperty("index.elasticSearch_port", Integer.toString(elasticsearch_port));
        }

        String elasticSearchClusterName = defaultprops.getProperty("index.elasticSearch_cluster");
        System.out.print(ANSI_YELLOW + "elasticSearchClusterName (" + elasticSearchClusterName + "): " + ANSI_RESET);
        elasticSearchClusterName = scanner.nextLine();
        if (!elasticSearchClusterName.isEmpty()) {
            defaultprops.setProperty("index.elasticSearch_cluster", elasticSearchClusterName);
        }

        String elasticSearch_TeisIndexName = defaultprops.getProperty("index.elasticSearch_TeisIndexName");
        System.out.print(ANSI_YELLOW + "elasticSearch_TeisIndexName (" + elasticSearch_TeisIndexName + "): " + ANSI_RESET);
        elasticSearch_TeisIndexName = scanner.nextLine();
        if (!elasticSearch_TeisIndexName.isEmpty()) {
            defaultprops.setProperty("index.elasticSearch_TeisIndexName", elasticSearch_TeisIndexName);
        }

        String elasticSearch_TeisTypeName = defaultprops.getProperty("index.elasticSearch_TeisTypeName");
        System.out.print(ANSI_YELLOW + "elasticSearch_TeisTypeName (" + elasticSearch_TeisTypeName + "): " + ANSI_RESET);
        elasticSearch_TeisTypeName = scanner.nextLine();
        if (!elasticSearch_TeisTypeName.isEmpty()) {
            defaultprops.setProperty("index.elasticSearch_TeisTypeName", elasticSearch_TeisTypeName);
        }

        String elasticsearch_nerdAnnot_indexName = defaultprops.getProperty("index.elasticSearch_nerdAnnotsIndexName");
        System.out.print(ANSI_YELLOW + "elasticsearch_nerdAnnot_indexName (" + elasticsearch_nerdAnnot_indexName + "): " + ANSI_RESET);
        elasticsearch_nerdAnnot_indexName = scanner.nextLine();
        if (!elasticsearch_nerdAnnot_indexName.isEmpty()) {
            defaultprops.setProperty("index.elasticSearch_nerdAnnotsIndexName", elasticsearch_nerdAnnot_indexName);
        }

        String elasticsearch_keyterm_indexName = defaultprops.getProperty("index.elasticSearch_keytermAnnotsIndexName");
        System.out.print(ANSI_YELLOW + "elasticsearch_keyterm_indexName (" + elasticsearch_keyterm_indexName + "): " + ANSI_RESET);
        elasticsearch_keyterm_indexName = scanner.nextLine();
        if (!elasticsearch_keyterm_indexName.isEmpty()) {
            defaultprops.setProperty("index.elasticSearch_keytermAnnotsIndexName", elasticsearch_keyterm_indexName);
        }

        String elasticSearch_quantitiesAnnotsIndexName = defaultprops.getProperty("index.elasticSearch_quantitiesAnnotsIndexName");
        System.out.print(ANSI_YELLOW + "elasticSearch_quantitiesAnnotsIndexName (" + elasticSearch_quantitiesAnnotsIndexName + "): " + ANSI_RESET);
        elasticSearch_quantitiesAnnotsIndexName = scanner.nextLine();
        if (!elasticSearch_quantitiesAnnotsIndexName.isEmpty()) {
            defaultprops.setProperty("index.elasticSearch_quantitiesAnnotsIndexName", elasticSearch_quantitiesAnnotsIndexName);
        }

        String elasticsearch_kb_indexName = defaultprops.getProperty("index.elasticSearch_kbIndexName");
        System.out.print(ANSI_YELLOW + "elasticsearch_kb_indexName (" + elasticsearch_kb_indexName + "): " + ANSI_RESET);
        elasticsearch_kb_indexName = scanner.nextLine();
        if (!elasticsearch_kb_indexName.isEmpty()) {
            defaultprops.setProperty("index.elasticSearch_kbIndexName", elasticsearch_kb_indexName);
        }

        FileOutputStream out = new FileOutputStream(file.getAbsolutePath() + File.separator + "config" + File.separator + "anhalytics.properties");
        defaultprops.store(out, null);
        out.close();

        System.out.println(ANSI_GREEN + "CHECK IF EVERYTHING IS UP AND RUNNING (Status..)." + ANSI_RESET);
        System.out.println(ANSI_GREEN + "PERFECTO, WE'RE READY NOW." + ANSI_RESET);
    }

    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP ANHALYTICS_COMMONS\n");
        help.append("-h: displays help\n");
        help.append("-configure: Prepare the configuration\n");
        help.append("-prepare: Prepare the knowledge base, load the schema. \n");
        return help.toString();
    }

}
