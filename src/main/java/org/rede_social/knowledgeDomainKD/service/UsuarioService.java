package org.rede_social.knowledgeDomainKD.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.rede_social.knowledgeDomainKD.model.Usuario;
import org.rede_social.knowledgeDomainKD.model.UsuarioDTO;
import org.rede_social.knowledgeDomainKD.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository repository;

	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {

		Optional<Usuario> usuarioExistente = repository.findByEmail(usuario.getEmail());
		if (usuarioExistente.isPresent()) {
			return Optional.empty();
		} else {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String senhaCriptografada = encoder.encode(usuario.getSenha());
			usuario.setSenha(senhaCriptografada);
			return Optional.ofNullable(repository.save(usuario));
		}
	}

	public Optional<UsuarioDTO> logarUsuario(Optional<UsuarioDTO> usuarioLogin) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		Optional<Usuario> usuarioPresente = repository.findByEmail(usuarioLogin.get().getEmail());

		if (usuarioPresente.isPresent()) {
			if (encoder.matches(usuarioLogin.get().getSenha(), usuarioPresente.get().getSenha())) {
				String auth = usuarioLogin.get().getEmail() + ":" + usuarioLogin.get().getSenha();
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);

				usuarioLogin.get().setToken(authHeader);
				usuarioLogin.get().setId(usuarioPresente.get().getId());
				usuarioLogin.get().setNome(usuarioPresente.get().getNome());
				usuarioLogin.get().setFoto(usuarioPresente.get().getFoto());
				usuarioLogin.get().setTipo(usuarioPresente.get().getTipo());
				

				return usuarioLogin;
			}
		}
		return null;
	}
	
	public Optional<?> alterarUsuario(UsuarioDTO usuarioParaAlterar) {
		return repository.findById(usuarioParaAlterar.getId()).map(usuarioExistente -> {

			if(usuarioParaAlterar.getNome().isBlank()) {
				usuarioExistente.setNome(usuarioExistente.getNome());
			} else {
				usuarioExistente.setNome(usuarioParaAlterar.getNome());
			}
			
			usuarioExistente.setSenha(usuarioExistente.getSenha());
			usuarioExistente.setDescricao(usuarioParaAlterar.getDescricao());
			usuarioExistente.setFoto(usuarioParaAlterar.getFoto());
			usuarioExistente.setTipo(usuarioParaAlterar.getTipo());
			usuarioExistente.setBio(usuarioParaAlterar.getBio());
			return Optional.ofNullable(repository.save(usuarioExistente));
		}).orElseGet(() -> {
			return Optional.empty();
		});
	}

}
