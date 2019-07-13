package com.github.pzn;

import static com.github.pzn.Key.*;

import com.bol.crypt.CryptVault;
import com.bol.secure.CachedEncryptionEventListener;
import com.bol.secure.Encrypted;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.repository.MongoRepository;

@SpringBootApplication
@Configuration
public class Application implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	@Autowired
	private MyBeanRepository myBeanRepository;

	@Bean
	public CryptVault cryptVault() {
		return new CryptVault()
				.with256BitAesCbcPkcs5PaddingAnd128BitSaltKey(0, Base64.getDecoder().decode("hqHKBLV83LpCqzKpf8OvutbCs+O5wX5BPu3btWpEvXA="))
				.withDefaultKeyVersion(0);
	}

	@Bean
	public CachedEncryptionEventListener encryptionEventListener(CryptVault cryptVault) {
		return new CachedEncryptionEventListener(cryptVault);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		myBeanRepository.deleteAll();
		MyBean myBean = new MyBean();
		myBean.stuff = someKey(10, "front door");
		myBeanRepository.save(myBean);
		myBeanRepository.findAll().forEach(e -> log.info("{}", e));
	}
}

class MyBean {
	@Id
	String id;
	MyStuff stuff;

	@Override public String toString() {
		return "MyBean{" + "id='" + id + '\'' + ", stuff=" + stuff + '}';
	}
}

interface MyStuff {

}

class Key implements MyStuff {
	@Encrypted
	String lock;
	@Encrypted
	int weight;

	public static Key someKey(int weight, String lock) {
		Key key = new Key();
		key.weight = weight;
		key.lock = lock;
		return key;
	}

	@Override
	public String toString() {
		return "Key{" + "lock='" + lock + '\'' + ", weight=" + weight + '}';
	}
}

interface MyBeanRepository extends MongoRepository<MyBean, String> {

}
