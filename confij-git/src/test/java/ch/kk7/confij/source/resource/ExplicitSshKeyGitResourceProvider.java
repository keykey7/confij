package ch.kk7.confij.source.resource;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.net.URI;

@AllArgsConstructor
public class ExplicitSshKeyGitResourceProvider extends GitResourceProvider {
	private final File privateKeyFile;
	private final File knownHostsFile;

	@Override
	protected GitSettings uriToGitSettings(URI uri) {
		return super.uriToGitSettings(uri)
				.withTransportConfigCallback(transport -> {
					SshTransport sshTransport = (SshTransport) transport;
					sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
						@Override
						protected void configure(Host host, Session session) {
							// nop
						}

						@Override
						protected JSch createDefaultJSch(FS fs) throws JSchException {
							JSch defaultJSch = super.createDefaultJSch(fs);
							defaultJSch.removeAllIdentity();
							defaultJSch.addIdentity(privateKeyFile.getAbsolutePath());
							defaultJSch.setKnownHosts(knownHostsFile.getAbsolutePath());
							return defaultJSch;
						}
					});
				});
	}
}
