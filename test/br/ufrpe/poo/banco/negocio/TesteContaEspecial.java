package br.ufrpe.poo.banco.negocio;

import static org.junit.Assert.*;

import org.junit.Test;

public class TesteContaEspecial {

	/**
	 * Testa se os parametros do construtor sao passados corretamente
	 */
	@Test
	public void construtorInicializaCorretamente() {
		ContaEspecial c = new ContaEspecial("1",100);
		assertEquals("Numero incorreto", "1", c.getNumero());
		assertEquals("Saldo incorreto", 100,  c.getSaldo(), 0);
		assertEquals("Bonus incorreto", 0,  c.getBonus(), 0);
	}
	
	
	/**
	 * Testa que a opera��o de credito modifica o bonus
	 */
	@Test
	public void creditarAumentaBonus() {
		ContaEspecial c = new ContaEspecial("1",900);
		c.creditar(100);
		assertEquals(1, c.getBonus(), 0);
		assertEquals(1000, c.getSaldo(), 0);
	}

	/**
	 * Testa que a opera��o de render bonus credita na conta o valor de bonus e zera o bonus
	 */
	@Test
	public void renderBonusAumentaSaldo() {
		ContaEspecial c = new ContaEspecial("6564",2000);
		assertEquals(0, c.getBonus(), 0);
		c.creditar(100);
		c.renderBonus();
		assertEquals(2101, c.getSaldo(), 0);
		assertEquals(0, c.getBonus(), 0);
	}	

}
