package org.apache.hadoop.hbase.zookeeper;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ConnectStringParser;
import org.apache.zookeeper.client.HostProvider;
import org.apache.zookeeper.client.StaticHostProvider;
import org.apache.zookeeper.common.PathUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @author leo.jie (weixiao.me@aliyun.com)
 */
public class TestZookeeperClient {
    @Test
    public void testClient() {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set(HConstants.ZOOKEEPER_QUORUM,"localhost");
        configuration.set(HConstants.ZOOKEEPER_CLIENT_PORT,"2181");
        try {
            ZKWatcher watcher = new ZKWatcher(configuration, TestZookeeperClient.class.getName(), null);
            System.out.println(ZKUtil.checkExists(watcher, "/brokers"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testClient2() {
        int DEFAULT_PORT = 2181;
        String connectString="localhost:2181/hbase";
        // parse out chroot, if any
        int off = connectString.indexOf('/');
        if (off >= 0) {
            String chrootPath = connectString.substring(off);
            // ignore "/" chroot spec, same as null
            if (chrootPath.length() == 1) {
                System.out.println("chroot is null");
            } else {
                PathUtils.validatePath(chrootPath);
                System.out.println(chrootPath);
            }
            connectString = connectString.substring(0, off);
        } else {
            System.out.println("chroot is null");
        }

        String hostsList[] = connectString.split(",");
        for (String host : hostsList) {
            int port = DEFAULT_PORT;
            int pidx = host.lastIndexOf(':');
            if (pidx >= 0) {
                // otherwise : is at the end of the string, ignore
                if (pidx < host.length() - 1) {
                    port = Integer.parseInt(host.substring(pidx + 1));
                }
                host = host.substring(0, pidx);
            }
            InetSocketAddress socketAddress = InetSocketAddress.createUnresolved(host, port);
            System.out.println(socketAddress);

        }



        String connectString2 = "localhost:2181";

        ConnectStringParser connectStringParser2 = new ConnectStringParser(connectString2);
        final ArrayList<InetSocketAddress> addresses = connectStringParser2.getServerAddresses();
        HostProvider hostProvider2 = new StaticHostProvider(addresses);
        System.out.println(hostProvider2);


        InetSocketAddress localhost1 =new InetSocketAddress("localhost", 2181);
        InetAddress address1 = localhost1.getAddress();

        InetSocketAddress localhost = InetSocketAddress.createUnresolved("127.0.0.1", 2181);
        InetAddress address = localhost.getAddress();
        //InetSocketAddress localhost3 = new InetSocketAddress("localhost", 2181);

        ConnectStringParser connectStringParser = new ConnectStringParser(connectString);
        ArrayList<InetSocketAddress> serverAddresses = connectStringParser.getServerAddresses();
        HostProvider hostProvider = new StaticHostProvider(connectStringParser.getServerAddresses());


        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost",2181);
         final CountDownLatch connectedSemaphore = new CountDownLatch(1);
        try {
            ZooKeeper zk = new ZooKeeper("localhost:2181", 6000,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            // 获取事件的状态
                            Event.KeeperState keeperState = event.getState();
                            Event.EventType eventType = event.getType();
                            // 如果是建立连接
                            if (Event.KeeperState.SyncConnected == keeperState) {
                                if (Event.EventType.None == eventType) {
                                    // 如果建立连接成功，则发送信号量，让后续阻塞程序向下执行
                                    System.out.println("zk 建立连接");
                                    connectedSemaphore.countDown();
                                }
                            }else {
                                System.out.println("failed");
                            }
                        }
                    });
            zk.exists("/hbase", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
