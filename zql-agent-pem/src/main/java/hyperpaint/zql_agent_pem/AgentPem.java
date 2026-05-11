package hyperpaint.zql_agent_pem;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.lang.instrument.Instrumentation;
import java.security.Security;

public class AgentPem {
    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("[Agent PEM] Initializing...");

        Security.addProvider(new BouncyCastleProvider());

        new AgentBuilder.Default()
                .type(ElementMatchers.named("org.apache.zookeeper.common.ClientX509Util"))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder
                                .method(ElementMatchers.named("createNettySslContextForClient").and(ElementMatchers.takesArguments(1)))
                                .intercept(MethodDelegation.to(MyClientX509Util.class))
                )
                .installOn(instrumentation);

        System.out.println("[Agent PEM] Initialized");
    }
}
