package br.ufrpe.poo.banco.negocio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import br.ufrpe.poo.banco.exceptions.ClienteJaPossuiContaException;
import br.ufrpe.poo.banco.exceptions.ClienteNaoPossuiContaException;

/**
 * Classe de teste respons�vel por testar as condi��es dos m�todos
 * adicionarConta e removerConta da classe Cliente.
 * 
 * @author Aluno
 * 
 */
public class TesteCliente {

	/**
	 * Testa a inser��o de uma nova conta vinculada ao cliente
	 * 
	 * @throws ClienteJaPossuiContaException
	 */
	@Test
	public void adicionaContasSucesso() throws ClienteJaPossuiContaException {
		Cliente c1 = new Cliente("nome", "123");
		c1.adicionarConta("2");
		c1.adicionarConta("1");
		assertEquals(c1.getContas().size(),2,0);
		assertEquals(c1.procurarConta("2"), 0);
		assertEquals(c1.procurarConta("1"), 1);
	}

	/**
	 * Testa a condi��o da tentativa de adicionar uma conta j� existente � lista de
	 * contas do cliente
	 * 
	 * @throws ClienteJaPossuiContaException
	 */
	@Test(expected = ClienteJaPossuiContaException.class)
	public void adicionarContaJaExistente() throws ClienteJaPossuiContaException {
		Cliente c1 = new Cliente("nome", "123");
		c1.adicionarConta("1"); 
		c1.adicionarConta("2"); 
		c1.adicionarConta("3"); 
		c1.adicionarConta("4"); 
		c1.adicionarConta("3"); // tentativa de adicionar a mesma conta
	}

	/**
	 * Teste a remo��o de uma conta da lista de contas do cliente
	 * 
	 * @throws ClienteJaPossuiContaException
	 * @throws ClienteNaoPossuiContaException
	 */
	@Test
	public void removerContaCliente() throws ClienteJaPossuiContaException, ClienteNaoPossuiContaException {
		Cliente c1 = new Cliente("nome", "123");
		c1.adicionarConta("1"); 
		c1.adicionarConta("2"); 
		c1.removerConta("2"); 
		c1.removerConta("1"); 
		assertEquals(c1.procurarConta("1"), -1);
		assertEquals(c1.procurarConta("2"), -1);
	}

	/**
	 * Testa a remo��o de uma determinada conta que n�o est� vinculada ao cliente
	 * 
	 * @throws ClienteNaoPossuiContaException
	 */
	@Test(expected = ClienteNaoPossuiContaException.class)
	public void removerContaClienteSemContaTest() throws ClienteNaoPossuiContaException {
		Cliente c1 = new Cliente("nome", "123");
		c1.removerConta("1"); 
	}

}
