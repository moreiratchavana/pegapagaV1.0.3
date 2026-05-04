import React, { useEffect, useRef, useState } from 'react';
import { Animated, Easing, SafeAreaView, StatusBar, StyleSheet, Text, TouchableOpacity, View } from 'react-native';

export default function Index() {
  const [etapa, setEtapa] = useState(1); // 1=teclado, 2=fingerprint, 3=pin, 4=sucesso
  const [rawValue, setRawValue] = useState('');
  const [pin, setPin] = useState('');
  const [aprovado, setAprovado] = useState(false);
  const [bloqueado, setBloqueado] = useState(false);

  const telaOpacidade = useRef(new Animated.Value(1)).current;
  const telaSlide = useRef(new Animated.Value(0)).current;
  const conteudoOpacidade = useRef(new Animated.Value(1)).current;
  const conteudoSlide = useRef(new Animated.Value(0)).current;

  const spinnerGiro = useRef(new Animated.Value(0)).current;
  const spinnerLoop = useRef<Animated.CompositeAnimation | null>(null);

  const checkEscala = useRef(new Animated.Value(0)).current;
  const sucessoOpacidade = useRef(new Animated.Value(0)).current;

  // Formato do teclado: "1,250 MT" (vírgula, sem decimais)
  const formatarTeclado = (val: string) => {
    if (!val) return '0';
    const num = parseInt(val, 10) || 0;
    return num.toLocaleString('pt-MZ').replace(/\s/g, '.');
  };

  // Formato MZN: "MZN 1.250,00"
  const formatarMZN = (val: string) => {
    if (!val) return '0,00';
    const num = parseInt(val, 10) || 0;
    return (num / 100).toLocaleString('pt-MZ', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  };

  const adicionarDigito = (d: string) => {
    if (rawValue.length >= 11) return;
    setRawValue(p => p + d);
  };
  const removerDigito = () => setRawValue(p => p.slice(0, -1));
  const adicionarPin = (d: string) => {
    if (pin.length >= 4) return;
    setPin(p => p + d);
  };
  const removerPin = () => setPin(p => p.slice(0, -1));

  useEffect(() => {
    if (etapa !== 2 && spinnerLoop.current) {
      spinnerLoop.current.stop();
      spinnerGiro.setValue(0);
    }

    if (etapa === 1) return;

    conteudoOpacidade.setValue(0);
    conteudoSlide.setValue(20);
    Animated.parallel([
      Animated.timing(conteudoOpacidade, { toValue: 1, duration: 350, useNativeDriver: true }),
      Animated.timing(conteudoSlide, { toValue: 0, duration: 350, easing: Easing.out(Easing.cubic), useNativeDriver: true }),
    ]).start();

    if (etapa === 2) {
      setAprovado(false);
      spinnerLoop.current = Animated.loop(
        Animated.timing(spinnerGiro, {
          toValue: 1,
          duration: 1200,
          easing: Easing.linear,
          useNativeDriver: true,
        })
      );
      spinnerLoop.current.start();
    }

    if (etapa === 4) {
      checkEscala.setValue(0);
      sucessoOpacidade.setValue(0);
      Animated.sequence([
        Animated.delay(200),
        Animated.spring(checkEscala, { toValue: 1, friction: 4, tension: 40, useNativeDriver: true }),
        Animated.timing(sucessoOpacidade, { toValue: 1, duration: 350, useNativeDriver: true }),
      ]).start();
    }
  }, [etapa]);

  useEffect(() => {
    if (etapa === 3 && pin.length === 4) {
      const t = setTimeout(() => irPara(4), 300);
      return () => clearTimeout(t);
    }
  }, [pin, etapa]);

  const irPara = (novaEtapa: number) => {
    if (bloqueado) return;
    setBloqueado(true);
    Animated.parallel([
      Animated.timing(telaOpacidade, { toValue: 0, duration: 140, useNativeDriver: true }),
      Animated.timing(telaSlide, { toValue: -8, duration: 140, useNativeDriver: true }),
    ]).start(() => {
      if (novaEtapa === 3) setPin('');
      setEtapa(novaEtapa);
      telaSlide.setValue(8);
      Animated.parallel([
        Animated.timing(telaOpacidade, { toValue: 1, duration: 240, useNativeDriver: true }),
        Animated.timing(telaSlide, { toValue: 0, duration: 240, useNativeDriver: true }),
      ]).start(() => setBloqueado(false));
    });
  };

  const handleFingerprintTap = () => {
    if (spinnerLoop.current) {
      spinnerLoop.current.stop();
      spinnerGiro.setValue(0);
    }
    setAprovado(true);
  };

  const Spinner = () => (
    <Animated.View style={{
      transform: [{
        rotate: spinnerGiro.interpolate({
          inputRange: [0, 1],
          outputRange: ['0deg', '360deg'],
        }),
      }],
    }}>
      <View style={s.spinnerContainer}>
        {[0, 45, 90, 135, 180, 225, 270, 315].map((graus, i) => (
          <View
            key={i}
            style={[
              s.spinnerSegmento,
              { transform: [{ rotate: `${graus}deg` }] },
              i % 2 === 0 ? s.spinnerOpaco : s.spinnerTransparente,
            ]}
          />
        ))}
      </View>
    </Animated.View>
  );

  return (
    <SafeAreaView style={[s.container, etapa === 2 && s.containerBranco, etapa === 4 && s.containerVerde]}>
      <StatusBar
        barStyle={etapa === 2 ? 'dark-content' : 'light-content'}
        backgroundColor={etapa === 2 ? '#FFFFFF' : etapa === 4 ? '#28a745' : '#0C0C0C'}
      />

      <Animated.View style={[s.telaWrapper, {
        opacity: telaOpacidade,
        transform: [{ translateY: telaSlide }],
      }]}>

        {/* ========== ETAPA 1: TECLADO ========== */}
        {etapa === 1 && (
          <View style={s.tela}>
            {/* Header */}
            <View style={s.headerTeclado}>
              <Text style={s.headerTitulo}>Novo Pedido</Text>
              <View style={s.terminalAtivo}>
                <View style={s.terminalDot} />
                <Text style={s.terminalTexto}>TERMINAL ATIVO</Text>
              </View>
            </View>

            {/* Display */}
            <View style={s.displayArea}>
              <Text style={s.displayLabel}>VALOR A COBRAR</Text>
              <View style={s.displayLinha}>
                <Text style={s.displayValor}>{formatarTeclado(rawValue)}</Text>
                <Text style={s.displayMt}>MT</Text>
              </View>
            </View>

            {/* Teclado */}
            <View style={s.tecladoArea}>
              {[['1','2','3'],['4','5','6'],['7','8','9'],['','0','⌫']].map((linha, ri) => (
                <View key={ri} style={s.tecladoLinha}>
                  {linha.map((tecla, ci) => (
                    <TouchableOpacity
                      key={ci}
                      style={[s.tecla, !tecla && { backgroundColor: 'transparent', borderWidth: 0 }]}
                      activeOpacity={0.5}
                      disabled={!tecla}
                      onPress={() => {
                        if (tecla === '⌫') removerDigito();
                        else adicionarDigito(tecla);
                      }}
                    >
                      <Text style={[s.teclaTexto, !tecla && { color: 'transparent' }]}>{tecla}</Text>
                    </TouchableOpacity>
                  ))}
                </View>
              ))}
            </View>

            {/* Botão Cobrar */}
            <View style={s.botaoArea}>
              <TouchableOpacity
                style={[s.botaoCobrar, rawValue.length === 0 && s.botaoCobrarOff]}
                activeOpacity={0.8}
                disabled={rawValue.length === 0}
                onPress={() => irPara(2)}
              >
                <Text style={[s.botaoCobrarTexto, rawValue.length === 0 && s.botaoCobrarTextoOff]}>
                  Cobrar
                </Text>
              </TouchableOpacity>
            </View>
          </View>
        )}

        {/* ========== ETAPA 2: FINGERPRINT TELA BRANCA ========== */}
        {etapa === 2 && (
          <View style={s.telaBranca}>
            <View style={s.topBarBranco}>
              <View style={s.topBarLineBranco} />
            </View>

            <Animated.View style={[s.fpContentBranco, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.fpCobrancaTitulo}>Cobrança</Text>
              <Text style={s.fpCobrancaValor}>MZN {formatarMZN(rawValue)}</Text>

              <View style={s.fpSpinnerArea}>
                {!aprovado ? (
                  <TouchableOpacity activeOpacity={0.8} onPress={handleFingerprintTap}>
                    <Spinner />
                  </TouchableOpacity>
                ) : (
                  <View style={s.fpCheckContainer}>
                    <View style={s.fpCheckCircle}>
                      <Text style={s.fpCheckIcon}>✓</Text>
                    </View>
                  </View>
                )}
              </View>

              <Text style={s.fpInstrucao}>
                {!aprovado
                  ? 'Toque com o dedo para confirmar'
                  : 'Identidade confirmada'}
              </Text>
            </Animated.View>

            <Animated.View style={[s.fpBotoesBranco, { opacity: conteudoOpacidade }]}>
              <TouchableOpacity
                style={[s.fpBotaoVermelho, !aprovado && s.fpBotaoVermelhoOff]}
                activeOpacity={0.8}
                disabled={!aprovado}
                onPress={() => irPara(3)}
              >
                <Text style={[s.fpBotaoVermelhoTexto, !aprovado && s.fpBotaoVermelhoTextoOff]}>
                  {!aprovado ? 'aguardando aprovação...' : 'Confirmado'}
                </Text>
              </TouchableOpacity>
              <TouchableOpacity style={s.fpBotaoOutline} activeOpacity={0.7}>
                <Text style={s.fpBotaoOutlineTexto}>Enviar Link de Pagamento</Text>
              </TouchableOpacity>
              <TouchableOpacity style={s.fpBotaoCancelar} activeOpacity={0.7} onPress={() => irPara(1)}>
                <Text style={s.fpBotaoCancelarTexto}>Cancelar</Text>
              </TouchableOpacity>
            </Animated.View>
          </View>
        )}

        {/* ========== ETAPA 3: PIN ========== */}
        {etapa === 3 && (
          <View style={s.tela}>
            <View style={s.topBar}>
              <View style={s.topBarLine} />
            </View>
            <Animated.View style={[s.centerContent, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.valorTopo}>MZN {formatarMZN(rawValue)}</Text>
              <Text style={s.pinTitulo}>Insira o PIN</Text>
              <View style={s.pinDots}>
                {[0,1,2,3].map(i => (
                  <View key={i} style={[s.pinDot, pin.length > i && s.pinDotCheio]} />
                ))}
              </View>
            </Animated.View>
            <Animated.View style={{ opacity: conteudoOpacidade }}>
              <View style={s.tecladoArea}>
                {[['1','2','3'],['4','5','6'],['7','8','9'],['','0','⌫']].map((linha, ri) => (
                  <View key={ri} style={s.tecladoLinha}>
                    {linha.map((tecla, ci) => (
                      <TouchableOpacity
                        key={ci}
                        style={[s.tecla, !tecla && { backgroundColor: 'transparent', borderWidth: 0 }]}
                        activeOpacity={0.5}
                        disabled={!tecla}
                        onPress={() => {
                          if (tecla === '⌫') removerPin();
                          else adicionarPin(tecla);
                        }}
                      >
                        <Text style={[s.teclaTexto, !tecla && { color: 'transparent' }]}>{tecla}</Text>
                      </TouchableOpacity>
                    ))}
                  </View>
                ))}
              </View>
            </Animated.View>
          </View>
        )}

        {/* ========== ETAPA 4: SUCESSO VERDE ========== */}
        {etapa === 4 && (
          <View style={s.telaVerde}>
            <View style={s.topBarVerde}>
              <View style={s.topBarLineVerde} />
            </View>
            <Animated.View style={[s.centerContentVerde, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.comprovanteTitulo}>Comprovante</Text>
              <Animated.View style={{ transform: [{ scale: checkEscala }] }}>
                <View style={s.checkCircleVerde}>
                  <Text style={s.checkIconVerde}>✓</Text>
                </View>
              </Animated.View>
              <Animated.View style={[s.sucessoConteudoVerde, { opacity: sucessoOpacidade }]}>
                <Text style={s.sucessoTituloVerde}>Pagamento Autorizado</Text>
                <Text style={s.sucessoSubtituloVerde}>Via M-Pesa</Text>
                <View style={s.sucessoDivisorVerde} />
                <Text style={s.sucessoValorVerde}>MZN {formatarMZN(rawValue)}</Text>
              </Animated.View>
            </Animated.View>
            <Animated.View style={[s.sucessoBotoesVerde, { opacity: sucessoOpacidade }]}>
              <TouchableOpacity
                style={s.botaoOutlineVerde}
                activeOpacity={0.7}
                onPress={() => { setRawValue(''); irPara(1); }}
              >
                <Text style={s.botaoOutlineTextoVerde}>Nova Venda</Text>
              </TouchableOpacity>
              <TouchableOpacity style={s.botaoBrancoVerde} activeOpacity={0.8}>
                <Text style={s.botaoBrancoTextoVerde}>Emitir Recibo</Text>
              </TouchableOpacity>
            </Animated.View>
          </View>
        )}

      </Animated.View>
    </SafeAreaView>
  );
}

const s = StyleSheet.create({
  // === CONTAINERS ===
  container: { flex: 1, backgroundColor: '#0C0C0C' },
  containerBranco: { backgroundColor: '#FFFFFF' },
  containerVerde: { backgroundColor: '#28a745' },
  telaWrapper: { flex: 1 },
  tela: {
    flex: 1,
    paddingHorizontal: 24,
    justifyContent: 'space-between',
    paddingVertical: 16,
  },
  telaBranca: {
    flex: 1,
    justifyContent: 'space-between',
    paddingVertical: 16,
  },
  telaVerde: {
    flex: 1,
    paddingHorizontal: 28,
    justifyContent: 'space-between',
    paddingVertical: 20,
  },
  centerContent: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  centerContentVerde: { flex: 1, justifyContent: 'center', alignItems: 'center' },

  // === TOP BAR ===
  topBar: { alignItems: 'center', paddingTop: 8, paddingBottom: 4 },
  topBarLine: { width: 40, height: 4, borderRadius: 2, backgroundColor: '#1F1F1F' },
  topBarBranco: { alignItems: 'center', paddingTop: 8, paddingBottom: 4 },
  topBarLineBranco: { width: 40, height: 4, borderRadius: 2, backgroundColor: '#E0E0E0' },
  topBarVerde: { alignItems: 'center', paddingTop: 8, paddingBottom: 4 },
  topBarLineVerde: { width: 40, height: 4, borderRadius: 2, backgroundColor: 'rgba(255,255,255,0.3)' },

  // ==============================
  // ETAPA 1: TECLADO (cópia fiel)
  // ==============================
  headerTeclado: {
    alignItems: 'center',
    marginTop: 8,
    marginBottom: 8,
  },
  headerTitulo: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: '700',
    letterSpacing: 0.5,
    marginBottom: 14,
  },
  terminalAtivo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: 'rgba(40, 167, 69, 0.12)',
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 20,
  },
  terminalDot: {
    width: 7,
    height: 7,
    borderRadius: 3.5,
    backgroundColor: '#28a745',
  },
  terminalTexto: {
    color: '#28a745',
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 1.2,
  },
  displayArea: {
    alignItems: 'center',
    marginTop: 8,
    marginBottom: 8,
    minHeight: 100,
    justifyContent: 'center',
  },
  displayLabel: {
    color: '#666666',
    fontSize: 13,
    fontWeight: '600',
    letterSpacing: 1.5,
    marginBottom: 10,
  },
  displayLinha: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 8,
  },
  displayValor: {
    color: '#FFFFFF',
    fontSize: 48,
    fontWeight: '300',
    fontFamily: 'monospace',
    letterSpacing: 2,
    lineHeight: 52,
  },
  displayMt: {
    color: '#555555',
    fontSize: 20,
    fontWeight: '500',
    marginBottom: 8,
    letterSpacing: 1,
  },
  tecladoArea: {
    flex: 1,
    justifyContent: 'center',
    maxHeight: 320,
  },
  tecladoLinha: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  tecla: {
    width: '29%',
    height: 58,
    backgroundColor: '#161616',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#222222',
  },
  teclaTexto: {
    color: '#FFFFFF',
    fontSize: 22,
    fontWeight: '400',
  },
  botaoArea: {
    marginBottom: 12,
  },
  botaoCobrar: {
    backgroundColor: '#EB3324',
    paddingVertical: 18,
    alignItems: 'center',
    borderRadius: 12,
    shadowColor: '#EB3324',
    shadowOffset: { width: 0, height: 5 },
    shadowOpacity: 0.35,
    shadowRadius: 12,
    elevation: 6,
  },
  botaoCobrarOff: {
    backgroundColor: '#1A1A1A',
    shadowOpacity: 0,
    elevation: 0,
    borderWidth: 1,
    borderColor: '#222222',
  },
  botaoCobrarTexto: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '700',
    letterSpacing: 1,
  },
  botaoCobrarTextoOff: {
    color: '#333333',
  },

  // ==============================
  // ETAPA 2: FINGERPRINT BRANCO
  // ==============================
  fpContentBranco: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 32,
  },
  fpCobrancaTitulo: {
    color: '#1A1A1A',
    fontSize: 20,
    fontWeight: '700',
    letterSpacing: 0.5,
    marginBottom: 12,
  },
  fpCobrancaValor: {
    color: '#1A1A1A',
    fontSize: 32,
    fontWeight: '700',
    fontFamily: 'monospace',
    letterSpacing: 1,
    marginBottom: 48,
  },
  fpSpinnerArea: {
    width: 64,
    height: 64,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 24,
  },
  spinnerContainer: {
    width: 64,
    height: 64,
    justifyContent: 'center',
    alignItems: 'center',
  },
  spinnerSegmento: {
    position: 'absolute',
    width: 6,
    height: 18,
    borderRadius: 3,
    backgroundColor: '#EB3324',
    top: 2,
    left: 29,
    transformOrigin: '3px 30px',
  },
  spinnerOpaco: { opacity: 1 },
  spinnerTransparente: { opacity: 0.15 },
  fpCheckContainer: {
    width: 64,
    height: 64,
    justifyContent: 'center',
    alignItems: 'center',
  },
  fpCheckCircle: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#28a745',
    justifyContent: 'center',
    alignItems: 'center',
  },
  fpCheckIcon: {
    color: '#FFFFFF',
    fontSize: 32,
    fontWeight: '700',
  },
  fpInstrucao: {
    color: '#888888',
    fontSize: 15,
    fontWeight: '400',
    textAlign: 'center',
    lineHeight: 22,
  },
  fpBotoesBranco: {
    width: '100%',
    paddingHorizontal: 28,
    gap: 10,
  },
  fpBotaoVermelho: {
    backgroundColor: '#EB3324',
    paddingVertical: 16,
    alignItems: 'center',
    borderRadius: 12,
    shadowColor: '#EB3324',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 10,
    elevation: 5,
  },
  fpBotaoVermelhoOff: {
    backgroundColor: '#B0B0B0',
    shadowOpacity: 0,
    elevation: 0,
  },
  fpBotaoVermelhoTexto: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
    letterSpacing: 0.3,
  },
  fpBotaoVermelhoTextoOff: {
    color: '#E0E0E0',
    fontSize: 15,
    fontWeight: '500',
  },
  fpBotaoOutline: {
    borderWidth: 2,
    borderColor: '#EB3324',
    backgroundColor: 'transparent',
    paddingVertical: 14,
    alignItems: 'center',
    borderRadius: 12,
  },
  fpBotaoOutlineTexto: {
    color: '#EB3324',
    fontSize: 15,
    fontWeight: '700',
    letterSpacing: 0.3,
  },
  fpBotaoCancelar: {
    paddingVertical: 14,
    alignItems: 'center',
  },
  fpBotaoCancelarTexto: {
    color: '#AAAAAA',
    fontSize: 14,
    fontWeight: '500',
  },

  // ==============================
  // ETAPA 3: PIN
  // ==============================
  valorTopo: {
    color: '#FFFFFF',
    fontSize: 18,
    fontFamily: 'monospace',
    opacity: 0.7,
    marginBottom: 50,
    letterSpacing: 1,
  },
  pinTitulo: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: '600',
    marginBottom: 36,
  },
  pinDots: { flexDirection: 'row', gap: 22 },
  pinDot: {
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 2,
    borderColor: '#333333',
    backgroundColor: 'transparent',
  },
  pinDotCheio: {
    backgroundColor: '#EB3324',
    borderColor: '#EB3324',
  },

  // ==============================
  // ETAPA 4: SUCESSO VERDE
  // ==============================
  comprovanteTitulo: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
    letterSpacing: 1,
    opacity: 0.8,
    marginBottom: 36,
  },
  checkCircleVerde: {
    width: 90,
    height: 90,
    borderRadius: 45,
    backgroundColor: '#FFFFFF',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 28,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  checkIconVerde: {
    color: '#28a745',
    fontSize: 44,
    fontWeight: '700',
  },
  sucessoConteudoVerde: { alignItems: 'center' },
  sucessoTituloVerde: {
    color: '#FFFFFF',
    fontSize: 22,
    fontWeight: '700',
    letterSpacing: 0.3,
  },
  sucessoSubtituloVerde: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '500',
    opacity: 0.85,
    marginTop: 2,
  },
  sucessoDivisorVerde: {
    width: 50,
    height: 1.5,
    backgroundColor: 'rgba(255,255,255,0.3)',
    marginVertical: 20,
    borderRadius: 1,
  },
  sucessoValorVerde: {
    color: '#FFFFFF',
    fontSize: 28,
    fontWeight: '700',
    fontFamily: 'monospace',
    letterSpacing: 1,
  },
  sucessoBotoesVerde: {
    width: '100%',
    gap: 12,
    marginBottom: 16,
  },
  botaoOutlineVerde: {
    borderWidth: 2,
    borderColor: '#FFFFFF',
    backgroundColor: 'transparent',
    paddingVertical: 16,
    alignItems: 'center',
    borderRadius: 14,
  },
  botaoOutlineTextoVerde: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  botaoBrancoVerde: {
    backgroundColor: '#FFFFFF',
    paddingVertical: 16,
    alignItems: 'center',
    borderRadius: 14,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.1,
    shadowRadius: 6,
    elevation: 3,
  },
  botaoBrancoTextoVerde: {
    color: '#28a745',
    fontSize: 16,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
});