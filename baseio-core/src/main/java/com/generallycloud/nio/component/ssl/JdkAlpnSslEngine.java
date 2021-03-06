package com.generallycloud.nio.component.ssl;

import java.util.LinkedHashSet;
import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.alpn.ALPN.ClientProvider;
import org.eclipse.jetty.alpn.ALPN.ServerProvider;

import com.generallycloud.nio.component.ssl.JdkApplicationProtocolNegotiator.ProtocolSelectionListener;
import com.generallycloud.nio.component.ssl.JdkApplicationProtocolNegotiator.ProtocolSelector;

final class JdkAlpnSslEngine extends JdkSslEngine {
	private static boolean	available;

	static boolean isAvailable() {
		updateAvailability();
		return available;
	}

	private static void updateAvailability() {
		if (available) {
			return;
		}

		try {
			// Always use bootstrap class loader.
			Class.forName("sun.security.ssl.ALPNExtension", true, null);
			available = true;
		} catch (Exception ignore) {
			// alpn-boot was not loaded.
		}
	}

	JdkAlpnSslEngine(SSLEngine engine, final JdkApplicationProtocolNegotiator applicationNegotiator, boolean server) {
		super(engine);

		if (server) {
			final ProtocolSelector protocolSelector = applicationNegotiator.protocolSelectorFactory().newSelector(
					this, new LinkedHashSet<String>(applicationNegotiator.protocols()));
			ALPN.put(engine, new ServerProvider() {
				@Override
				public String select(List<String> protocols) throws SSLException {
					try {
						return protocolSelector.select(protocols);
					} catch (SSLHandshakeException e) {
						throw e;
					} catch (Throwable t) {
						SSLHandshakeException e = new SSLHandshakeException(t.getMessage());
						e.initCause(t);
						throw e;
					}
				}

				@Override
				public void unsupported() {
					protocolSelector.unsupported();
				}
			});
		} else {
			final ProtocolSelectionListener protocolListener = applicationNegotiator.protocolListenerFactory()
					.newListener(this, applicationNegotiator.protocols());
			ALPN.put(engine, new ClientProvider() {
				@Override
				public List<String> protocols() {
					return applicationNegotiator.protocols();
				}

				@Override
				public void selected(String protocol) throws SSLException {
					try {
						protocolListener.selected(protocol);
					} catch (SSLHandshakeException e) {
						throw e;
					} catch (Throwable t) {
						SSLHandshakeException e = new SSLHandshakeException(t.getMessage());
						e.initCause(t);
						throw e;
					}
				}

				@Override
				public void unsupported() {
					protocolListener.unsupported();
				}
			});
		}
	}

	@Override
	public void closeInbound() throws SSLException {
		ALPN.remove(getWrappedEngine());
		super.closeInbound();
	}

	@Override
	public void closeOutbound() {
		ALPN.remove(getWrappedEngine());
		super.closeOutbound();
	}
}
