/*
 *  Copyright 2009-2016 Weibo, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.zjzcn.ceper.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 网络工具类
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-28
 */

public class NetUtils {

	private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

	public static final String LOCALHOST = "127.0.0.1";
	public static final String ANYHOST = "0.0.0.0";
	private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
	private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");
	private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

	private static volatile InetAddress localAddress = null;
	
	public static boolean isInvalidLocalHost(String host) {
		return host == null || host.length() == 0 || host.equalsIgnoreCase("localhost") || host.equals("0.0.0.0")
				|| (LOCAL_IP_PATTERN.matcher(host).matches());
	}

	public static boolean isValidLocalHost(String host) {
		return !isInvalidLocalHost(host);
	}

	/**
	 * {@link #getLocalAddress(Map)}
	 * 
	 * @return
	 */
	public static InetAddress getLocalAddress() {
		return getLocalAddress(null);
	}

	/**
	 * <pre>
	 * 查找策略：首先看是否已经查到ip --> hostname对应的ip --> 根据连接目标端口得到的本地ip --> 轮询网卡
	 * </pre>
	 * 
	 * @return local ip
	 */
	public static InetAddress getLocalAddress(Map<String, Integer> destHostPorts) {
		if (localAddress != null) {
			return localAddress;
		}

		InetAddress localAddr = getLocalAddressByHostname();
		if (!isValidIp(localAddr)) {
			localAddr = getLocalAddressBySocket(destHostPorts);
		}

		if (!isValidIp(localAddr)) {
			localAddr = getLocalAddressByNetworkInterface();
		}

		if (isValidIp(localAddr)) {
			localAddress = localAddr;
		}

		return localAddr;
	}

	/**
	 * ip:port
	 * @param address
	 * @return
	 */
	public static boolean isValidAddress(String address) {
		return ADDRESS_PATTERN.matcher(address).matches();
	}

	/**
	 * ip
	 * @param address
	 * @return
	 */
	public static boolean isValidIp(InetAddress address) {
		if (address == null || address.isLoopbackAddress())
			return false;
		String name = address.getHostAddress();
		return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
	}

	public static String getHostName(SocketAddress socketAddress) {
		if (socketAddress == null) {
			return null;
		}

		if (socketAddress instanceof InetSocketAddress) {
			return ((InetSocketAddress) socketAddress).getHostName();
		}

		return null;
	}

	// ------------------private----------------------
	private static InetAddress getLocalAddressByHostname() {
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (isValidIp(localAddress)) {
				return localAddress;
			}
		} catch (Throwable e) {
			logger.warn("Failed to retriving local address by hostname: ", e);
		}
		return null;
	}

	private static InetAddress getLocalAddressBySocket(Map<String, Integer> remoteHostPorts) {
		if (remoteHostPorts == null || remoteHostPorts.size() == 0) {
			return null;
		}

		for (Map.Entry<String, Integer> entry : remoteHostPorts.entrySet()) {
			String remoteHost = entry.getKey();
			int remotePort = entry.getValue();
			try {
				Socket socket = new Socket();
				try {
					SocketAddress addr = new InetSocketAddress(remoteHost, remotePort);
					socket.connect(addr, 1000);
					return socket.getLocalAddress();
				} finally {
					try {
						socket.close();
					} catch (Throwable e) {
					}
				}
			} catch (Exception e) {
				logger.warn(String.format("Failed to get local address by connecting to remote host:port(%s:%s).",
						remoteHost, remotePort), e);
			}
		}
		return null;
	}

	private static InetAddress getLocalAddressByNetworkInterface() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces != null) {
				while (interfaces.hasMoreElements()) {
					try {
						NetworkInterface network = interfaces.nextElement();
						Enumeration<InetAddress> addresses = network.getInetAddresses();
						while (addresses.hasMoreElements()) {
							try {
								InetAddress address = addresses.nextElement();
								if (isValidIp(address)) {
									return address;
								}
							} catch (Throwable e) {
								logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
							}
						}
					} catch (Throwable e) {
						logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
					}
				}
			}
		} catch (Throwable e) {
			logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
		}
		return null;
	}

}
