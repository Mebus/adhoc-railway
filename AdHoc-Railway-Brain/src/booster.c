/*
 * booser.c
 *
 *  Created on: 13.03.2012
 *      Author: fork
 */

#include "uart_interrupt.h"
#include "spi.h"
#include "booster.h"
#include "global.h"
#include "debug.h"

void init_boosters() {
	for (int i = 0; i < BOOSTER_COUNT; i++) {
		booster_state[i].active = 0;
		booster_state[i].shortcut = 0;
	}

	stop_all_boosters();
}
void report_boosterstate() {
	uart_puts("XBS ");
	for (int i = 0; i < BOOSTER_COUNT; i++) {
		if (booster_state[i].active)
			uart_puts("A ");
		else if (booster_state[i].shortcut) {
			uart_puts("S ");
		} else {
			uart_puts("O ");
		}
	}
	uart_putc('\r');
	uart_flush();
}
void check_shorts() {

	uint8_t shortDetected = 0;
	unsigned char shorts = SPI_MasterReceiveShort();
	for (int i = 0; i < BOOSTER_COUNT; i++) {
		if ((shorts & (1 << i)) && booster_state[i].active == 1) {
			booster_state[i].shortcut = 1;
			stop_booster_short(i);
			shortDetected = 1;
		}
	}
	if(shortDetected)
		report_boosterstate();
}


void stop_booster(int nr) {

	booster_state[nr].active = 0;
	booster_state[nr].shortcut = 0;

	unsigned char stateForSPI = get_booster_spi_state();
#ifdef DEBUG_BOOSTER_STATE
	SPI_MasterTransmitDebug(stateForSPI);
#endif
	SPI_MasterTransmitGO(stateForSPI);
	report_boosterstate();
}
void stop_booster_short(int nr) {

	booster_state[nr].active = 0;
	unsigned char stateForSPI = get_booster_spi_state();

#ifdef DEBUG_BOOSTER_STATE
	SPI_MasterTransmitDebug(stateForSPI);
#endif
	SPI_MasterTransmitGO(stateForSPI);

}

void go_booster(int nr) {

	booster_state[nr].active = 1;
	booster_state[nr].shortcut = 0;

	unsigned char stateForSPI = get_booster_spi_state();
#ifdef DEBUG_BOOSTER_STATE
	SPI_MasterTransmitDebug(stateForSPI);
#endif
	SPI_MasterTransmitGO(stateForSPI);
	report_boosterstate();
}

void stop_all_boosters() {

	for (int i = 0; i < BOOSTER_COUNT; i++) {
		booster_state[i].active = 0;
		booster_state[i].shortcut = 0;
	}

	unsigned char stateForSPI = get_booster_spi_state();
#ifdef DEBUG_BOOSTER_STATE
	SPI_MasterTransmitDebug(stateForSPI);
#endif
	SPI_MasterTransmitGO(stateForSPI);
	replys("Pwr off");
	report_boosterstate();

}

void go_all_boosters() {

	for (int i = 0; i < BOOSTER_COUNT; i++) {
		booster_state[i].active = 1;
		booster_state[i].shortcut = 0;
	}

	unsigned char stateForSPI = get_booster_spi_state();
#ifdef DEBUG_BOOSTER_STATE
	SPI_MasterTransmitDebug(stateForSPI);
#endif
	SPI_MasterTransmitGO(stateForSPI);
	replys("Pwr on");
	report_boosterstate();
}
