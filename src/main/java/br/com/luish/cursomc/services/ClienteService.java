package br.com.luish.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.luish.cursomc.domain.Cidade;
import br.com.luish.cursomc.domain.Cliente;
import br.com.luish.cursomc.domain.Endereco;
import br.com.luish.cursomc.domain.enums.TipoCliente;
import br.com.luish.cursomc.dto.ClienteDTO;
import br.com.luish.cursomc.dto.ClienteNewDTO;
import br.com.luish.cursomc.repositories.ClienteRepository;
import br.com.luish.cursomc.repositories.EnderecoRepository;
import br.com.luish.cursomc.services.exceptions.DataIntegrityException;
import br.com.luish.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	public Cliente find(Integer id) {

		Optional<Cliente> obj = clienteRepository.findById(id);

		if (obj.isEmpty()) {
			throw new ObjectNotFoundException(
					"Objeto não encontardo! Id: " + id + ", Tipo: " + Cliente.class.getName());
		}

		return obj.orElse(null);

	}

	// mesma transação do db
	@Transactional
	public Cliente insert(Cliente obj) {

		obj.setId(null);
		obj = clienteRepository.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());

		return obj;

	}

	public Cliente update(Cliente obj) {

		Cliente newObj = find(obj.getId());

		updateData(newObj, obj);

		return clienteRepository.save(obj);

	}

	public void delete(Integer id) {

		find(id);

		try {
			clienteRepository.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há pedidos relacionados");
		}

	}

	public List<Cliente> findAll() {

		return clienteRepository.findAll();

	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {

		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);

		return clienteRepository.findAll(pageRequest);

	}

	public Cliente froDTO(ClienteDTO obj) {

		return new Cliente(obj.getId(), obj.getNome(), obj.getEmail(), null, null);

	}

	private void updateData(Cliente newObj, Cliente obj) {

		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());

	}

	public Cliente fromDTO(ClienteNewDTO objDTO) {

		Cliente cli = new Cliente(null, objDTO.getNome(), objDTO.getEmail(), objDTO.getCpfOuCnpj(),
				TipoCliente.toEnum(objDTO.getTipo()));
		Cidade cid = new Cidade(objDTO.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDTO.getLogradouro(), objDTO.getNumero(), objDTO.getComplemento(),
				objDTO.getBairro(), objDTO.getCep(), cli, cid);

		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDTO.getTelefone1());

		if (objDTO.getTelefone2() != null) {
			cli.getTelefones().add(objDTO.getTelefone2());
		}

		if (objDTO.getTelefone3() != null) {
			cli.getTelefones().add(objDTO.getTelefone3());
		}

		return cli;
	}

}
