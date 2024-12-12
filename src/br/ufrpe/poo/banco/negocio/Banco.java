package br.ufrpe.poo.banco.negocio;

import br.ufrpe.poo.banco.dados.IRepositorioClientes;
import br.ufrpe.poo.banco.dados.IRepositorioContas;
import br.ufrpe.poo.banco.dados.RepositorioClientesArquivoBin;
import br.ufrpe.poo.banco.dados.RepositorioContasArquivoBin;
import br.ufrpe.poo.banco.exceptions.AtualizacaoNaoRealizadaException;
import br.ufrpe.poo.banco.exceptions.ClienteJaCadastradoException;
import br.ufrpe.poo.banco.exceptions.ClienteJaPossuiContaException;
import br.ufrpe.poo.banco.exceptions.ClienteNaoCadastradoException;
import br.ufrpe.poo.banco.exceptions.ClienteNaoPossuiContaException;
import br.ufrpe.poo.banco.exceptions.ContaJaAssociadaException;
import br.ufrpe.poo.banco.exceptions.ContaJaCadastradaException;
import br.ufrpe.poo.banco.exceptions.ContaNaoEncontradaException;
import br.ufrpe.poo.banco.exceptions.InicializacaoSistemaException;
import br.ufrpe.poo.banco.exceptions.RenderBonusContaEspecialException;
import br.ufrpe.poo.banco.exceptions.RenderJurosPoupancaException;
import br.ufrpe.poo.banco.exceptions.RepositorioException;
import br.ufrpe.poo.banco.exceptions.SaldoInsuficienteException;
import br.ufrpe.poo.banco.exceptions.ValorInvalidoException;
import br.ufrpe.poo.banco.iterator.IteratorCliente;

/**
 * Implementacao do sistema bancario que faz a comunicacao com a persistencia e
 * a gui.
 * 
 * @author
 * 
 */
public class Banco implements IGerencia, ICliente {

	/**
	 * Instancia do repositorio de clientes.
	 */
	protected IRepositorioClientes clientes;

	/**
	 * Instacia do repositorio de contas
	 */
	protected IRepositorioContas contas;

	/**
	 * Instancia do comunicador.
	 */
	protected static Banco instance;

	protected Banco(IRepositorioClientes clientes, IRepositorioContas contas) {
		this.clientes = clientes;
		this.contas = contas;
	}

	/**
	 * Retorna a instancia unica do banco.
	 * 
	 * @return se o banco nao foi instanciado. Se o banco ja foi instanciado eh
	 *         retornado <code>null</code>.
	 * @throws RepositorioException          Lancada quando ocorre erro no
	 *                                       repositorio.
	 * @throws InicializacaoSistemaException Lancada quando ocorre erro no
	 *                                       repositorio.
	 */
	public static Banco getInstance() throws RepositorioException, InicializacaoSistemaException {
		if (Banco.instance == null) {
			try {
				Banco.instance = new Banco(new RepositorioClientesArquivoBin(), new RepositorioContasArquivoBin());
			} catch (RepositorioException e) {
				throw new InicializacaoSistemaException();
			}
		}
		return Banco.instance;
	}

	@Override
	public void cadastrarCliente(Cliente cliente) throws RepositorioException, ClienteJaCadastradoException {
		if (!this.clientes.inserir(cliente))
			throw new ClienteJaCadastradoException();
	}

	@Override
	public Cliente procurarCliente(String cpf) {
		return this.clientes.procurar(cpf);
	}

	@Override
	public void cadastrar(ContaAbstrata conta) throws RepositorioException, ContaJaCadastradaException {
		if (!this.contas.inserir(conta))
			throw new ContaJaCadastradaException();
	}

	@Override
	public ContaAbstrata procurarConta(String numero) {
		return this.contas.procurar(numero);
	}

	boolean contaJaAssociada(String numero) {
		IteratorCliente citerator = this.clientes.getIterator();
		while (citerator.hasNext()) {
			Cliente c = citerator.next();
			if (c.getContas().contains(numero))
				return true;
		}
		return false;
	}

	// ContaNaoExisteException
	@Override
	public void associarConta(String cpf, String numeroConta)
			throws ClienteNaoCadastradoException, ContaNaoEncontradaException, ClienteJaPossuiContaException,
			ContaJaAssociadaException, RepositorioException {
		// procura se conta existe
		ContaAbstrata conta = procurarConta(numeroConta);
		if (conta == null) {
			throw new ContaNaoEncontradaException();
		}
		// procura se cliente existe
		Cliente cliente = this.procurarCliente(cpf);
		if (cliente == null) {
			throw new ClienteNaoCadastradoException();
		}
		// verifica se conta já está associada com algum cliente
		if (this.contaJaAssociada(conta.getNumero())) {
			throw new ContaJaAssociadaException();
		}
		// associa conta ao cliente
		cliente.adicionarConta(numeroConta);
		// atualiza cliente no repositorio
		this.clientes.atualizar(cliente);
	}

	@Override
	public void removerCliente(String cpf) throws RepositorioException, ClienteNaoCadastradoException,
			ContaNaoEncontradaException, ClienteNaoPossuiContaException {
		// verifica a existência do cliente
		Cliente cliente = this.procurarCliente(cpf);
		if (cliente == null) {
			throw new ClienteNaoCadastradoException();
		}
		// remove as contas associadas ao cliente
		for (String numConta : cliente.getContas()) {
			this.removerConta(cliente, numConta);
		}
		// remove cliente do repositorio
		this.clientes.remover(cpf);
	}

	@Override
	public void removerConta(Cliente cliente, String numeroConta)
			throws RepositorioException, ContaNaoEncontradaException, ClienteNaoPossuiContaException {
		cliente.removerConta(numeroConta);
		if (!this.contas.remover(numeroConta))
			throw new ContaNaoEncontradaException();
		this.clientes.atualizar(cliente);
	}

	@Override
	public void creditar(ContaAbstrata conta, double valor) throws RepositorioException, ValorInvalidoException {
		if (valor < 0)
			throw new ValorInvalidoException();
		conta.creditar(valor);
	}

	@Override
	public void debitar(ContaAbstrata conta, double valor)
			throws RepositorioException, SaldoInsuficienteException, ValorInvalidoException {
		if (valor < 0)
			throw new ValorInvalidoException();
		if (this.contas.existe(conta.getNumero())) {
			conta.debitar(valor);
			this.contas.atualizar(conta);
		}
	}

	@Override
	public void transferir(ContaAbstrata contaOrigem, ContaAbstrata contaDestino, double valor)
			throws SaldoInsuficienteException, RepositorioException, ValorInvalidoException {
		if (this.contas.existe(contaOrigem.getNumero()) && this.contas.existe(contaDestino.getNumero())) {
			this.debitar(contaOrigem, valor);
			this.creditar(contaDestino, valor);
			this.contas.atualizar(contaOrigem);
			this.contas.atualizar(contaDestino);
		}
	}

	@Override
	public void atualizarCliente(Cliente cliente) throws RepositorioException, AtualizacaoNaoRealizadaException {
		if (!this.clientes.atualizar(cliente))
			throw new AtualizacaoNaoRealizadaException();
	}

	public void renderBonus(ContaAbstrata conta)
			throws RenderBonusContaEspecialException, RepositorioException, ContaNaoEncontradaException {
		if (conta instanceof ContaEspecial) {
			if (this.contas.existe(conta.getNumero())) {
				((ContaEspecial) conta).renderBonus();
				this.contas.atualizar(conta);
			} else {
				throw new ContaNaoEncontradaException();
			}
		} else {
			throw new RenderBonusContaEspecialException();
		}

	}

	public void renderJuros(ContaAbstrata conta)
			throws RenderJurosPoupancaException, ContaNaoEncontradaException, RepositorioException {
		if (conta instanceof Poupanca) {
			if (this.contas.existe(conta.getNumero())) {
				((Poupanca) conta).renderJuros(0.5);
				this.contas.atualizar(conta);
			} else {
				throw new ContaNaoEncontradaException();
			}
		} else {
			throw new RenderJurosPoupancaException();
		}
	}

}
