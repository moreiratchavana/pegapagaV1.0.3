import React, { useEffect, useRef, useState } from 'react';
import { Animated, Dimensions, Easing, SafeAreaView, StatusBar, StyleSheet, Text, TouchableOpacity, View } from 'react-native';

const { height } = Dimensions.get('window');

export default function Index() {
  const [etapa, setEtapa] = useState(1);
  const [valor, setValor] = useState("500.00");
  const [showVersion, setShowVersion] = useState(true);
  const [bloqueado, setBloqueado] = useState(false);

  // Splash
  const moveLogo = useRef(new Animated.Value(0)).current;
  const buttonFade = useRef(new Animated.Value(0)).current;

  // Transição entre telas
  const telaOpacidade = useRef(new Animated.Value(1)).current;
  const telaSlide = useRef(new Animated.Value(0)).current;

  // Conteúdo interno de cada tela
  const conteudoOpacidade = useRef(new Animated.Value(0)).current;
  const conteudoSlide = useRef(new Animated.Value(24)).current;

  // Fingerprint pulse
  const fpEscala = useRef(new Animated.Value(1)).current;
  const fpBrilho = useRef(new Animated.Value(0.4)).current;
  const fpLoop = useRef<Animated.CompositeAnimation | null>(null);

  // Checkmark
  const checkEscala = useRef(new Animated.Value(0)).current;

  // Confirmed amount slide
  const valorOpacidade = useRef(new Animated.Value(0)).current;
  const valorSlide = useRef(new Animated.Value(16)).current;

  // --- Splash ---
  useEffect(() => {
    Animated.sequence([
      Animated.delay(1),
      Animated.parallel([
        Animated.timing(moveLogo, {
          toValue: -height * -0.05,
          duration: 900,
          easing: Easing.out(Easing.exp),
          useNativeDriver: true,
        }),
        Animated.timing(buttonFade, {
          toValue: 1,
          duration: 900,
          useNativeDriver: true,
        }),
      ]),
    ]).start(() => setShowVersion(false));
  }, []);

  // --- Animações por etapa ---
  useEffect(() => {
    // Cancela loop do fingerprint se sair da etapa 6
    if (etapa !== 6 && fpLoop.current) {
      fpLoop.current.stop();
      fpEscala.setValue(1);
      fpBrilho.setValue(0.4);
    }

    if (etapa <= 2) return;

    // Reset + entrada do conteúdo
    conteudoOpacidade.setValue(0);
    conteudoSlide.setValue(24);
    Animated.parallel([
      Animated.timing(conteudoOpacidade, {
        toValue: 1,
        duration: 380,
        useNativeDriver: true,
      }),
      Animated.timing(conteudoSlide, {
        toValue: 0,
        duration: 380,
        easing: Easing.out(Easing.cubic),
        useNativeDriver: true,
      }),
    ]).start();

    // Fingerprint pulse (etapa 6)
    if (etapa === 6) {
      fpLoop.current = Animated.loop(
        Animated.sequence([
          Animated.parallel([
            Animated.timing(fpEscala, { toValue: 1.1, duration: 700, useNativeDriver: true }),
            Animated.timing(fpBrilho, { toValue: 0.15, duration: 700, useNativeDriver: true }),
          ]),
          Animated.parallel([
            Animated.timing(fpEscala, { toValue: 1, duration: 700, useNativeDriver: true }),
            Animated.timing(fpBrilho, { toValue: 0.4, duration: 700, useNativeDriver: true }),
          ]),
        ])
      );
      fpLoop.current.start();
    }

    // Checkmark bounce (etapa 8)
    if (etapa === 8) {
      checkEscala.setValue(0);
      valorOpacidade.setValue(0);
      valorSlide.setValue(16);
      Animated.sequence([
        Animated.delay(250),
        Animated.spring(checkEscala, {
          toValue: 1,
          friction: 4,
          tension: 50,
          useNativeDriver: true,
        }),
        Animated.parallel([
          Animated.timing(valorOpacidade, { toValue: 1, duration: 350, useNativeDriver: true }),
          Animated.timing(valorSlide, { toValue: 0, duration: 350, useNativeDriver: true }),
        ]),
      ]).start();
    }
  }, [etapa]);

  // --- Navegação com transição ---
  const irPara = (novaEtapa: number) => {
    if (bloqueado) return;
    setBloqueado(true);
    Animated.parallel([
      Animated.timing(telaOpacidade, { toValue: 0, duration: 160, useNativeDriver: true }),
      Animated.timing(telaSlide, { toValue: -12, duration: 160, useNativeDriver: true }),
    ]).start(() => {
      setEtapa(novaEtapa);
      telaSlide.setValue(12);
      Animated.parallel([
        Animated.timing(telaOpacidade, { toValue: 1, duration: 260, useNativeDriver: true }),
        Animated.timing(telaSlide, { toValue: 0, duration: 260, useNativeDriver: true }),
      ]).start(() => setBloqueado(false));
    });
  };

  // --- Componentes auxiliares ---
  const Header = () => (
    <View style={s.header}>
      <Text style={s.logoText}>Pega-Paga | M-Pesa</Text>
    </View>
  );

  const BotaoPrincipal = ({ children, onPress, estilo }: { children: React.ReactNode; onPress: () => void; estilo?: any }) => (
    <TouchableOpacity
      style={[s.mainButton, estilo]}
      activeOpacity={0.75}
      onPress={onPress}
    >
      <Text style={s.buttonText}>{children}</Text>
    </TouchableOpacity>
  );

  const BotaoOutline = ({ children, onPress, estilo }: { children: React.ReactNode; onPress: () => void; estilo?: any }) => (
    <TouchableOpacity
      style={[s.outlineButton, estilo]}
      activeOpacity={0.7}
      onPress={onPress}
    >
      <Text style={s.outlineButtonText}>{children}</Text>
    </TouchableOpacity>
  );

  // Ícone fingerprint com View pura
  const FingerprintIcone = () => (
    <View style={s.fpContainer}>
      {[44, 32, 20, 10].map((tam, i) => (
        <View
          key={i}
          style={{
            width: tam,
            height: tam,
            borderRadius: tam / 2,
            borderWidth: 2.5,
            borderColor: '#EB3324',
            position: 'absolute',
          }}
        />
      ))}
    </View>
  );

  // Ícone QR Code com View pura (Grid simulando um QR)
  const QrCodeIcone = () => {
    const pattern = [
      [1,1,1,0,1,0,1,1,1],
      [1,0,1,0,0,0,1,0,1],
      [1,1,1,0,1,0,1,1,1],
      [0,0,0,0,1,0,0,0,0],
      [1,0,1,1,0,1,1,0,1],
      [0,0,0,0,1,0,0,0,0],
      [1,1,1,0,0,0,1,1,1],
      [1,0,1,0,1,0,1,0,1],
      [1,1,1,0,1,0,1,1,1],
    ];
    return (
      <View style={s.qrWrapper}>
        {pattern.map((row, r) => (
          <View key={r} style={s.qrRow}>
            {row.map((cell, c) => (
              <View key={c} style={[s.qrCell, cell ? s.qrCellBlack : null]} />
            ))}
          </View>
        ))}
      </View>
    );
  };

  // ===================== RENDER =====================
  return (
    <SafeAreaView style={s.container}>
      <StatusBar barStyle="light-content" />

      <Animated.View style={[s.telaWrapper, {
        opacity: telaOpacidade,
        transform: [{ translateY: telaSlide }],
      }]}>

        {/* === ETAPA 1 & 2: Splash === */}
        {etapa <= 2 && (
          <View style={s.content}>
            <Animated.View style={[s.centerBox, { transform: [{ translateY: moveLogo }] }]}>
              <Text style={s.logoGrande}>Pega-Paga</Text>
              <Text style={s.logoSub}>| M-Pesa</Text>

              <Animated.View style={{ opacity: buttonFade, width: '100%', marginTop: 28 }}>
                <BotaoPrincipal onPress={() => irPara(4)}>
                  Começar Pagamento
                </BotaoPrincipal>
              </Animated.View>
            </Animated.View>

            <View style={s.footer}>
              {showVersion && <Text style={s.footerText}></Text>}
            </View>
          </View>
        )}

        {/* === ETAPA 4: Montante === */}
        {etapa === 4 && (
          <View style={s.fullWidth}>
            <Header />
            <Animated.View style={[s.stageContent, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.label}>Insira um Montante:</Text>
              <Text style={s.amountDisplay}>
                <Text style={s.currency}>MZN </Text>0.00
              </Text>

              {/* Teclado numérico simplificado */}
              <View style={s.teclado}>
                {[['1','2','3'],['4','5','6'],['7','8','9'],['','0','⌫']].map((linha, ri) => (
                  <View key={ri} style={s.tecladoLinha}>
                    {linha.map((tecla, ci) => (
                      <TouchableOpacity
                        key={ci}
                        style={[s.tecla, !tecla && { backgroundColor: 'transparent' }]}
                        activeOpacity={0.6}
                        disabled={!tecla}
                      >
                        <Text style={[s.teclaTexto, !tecla && { color: 'transparent' }]}>{tecla}</Text>
                      </TouchableOpacity>
                    ))}
                  </View>
                ))}
              </View>

              <BotaoPrincipal onPress={() => irPara(5)} estilo={{ marginTop: 'auto', marginBottom: 20, paddingHorizontal: 20 }}>
                Avançar
              </BotaoPrincipal>
            </Animated.View>
          </View>
        )}

        {/* === ETAPA 5: Método === */}
        {etapa === 5 && (
          <View style={s.fullWidth}>
            <Header />
            <Animated.View style={[s.stageContent, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.label}>Montante:</Text>
              <Text style={s.amountDisplay}>
                <Text style={s.currency}>MZN </Text>{valor}
              </Text>

              <Text style={s.subLabel}>Meios de Pagamento:</Text>

              <View style={s.row}>
                <BotaoOutline onPress={() => irPara(6)} estilo={s.halfBtn}>
                  Fingerprint
                </BotaoOutline>
                <BotaoPrincipal onPress={() => irPara(7)} estilo={s.halfBtn}>
                  PIN
                </BotaoPrincipal>
              </View>

              {/* Alterado para ir para a nova ETAPA 9 */}
              <BotaoOutline onPress={() => irPara(9)} estilo={{ width: '100%', marginTop: 10 }}>
                QR CODE
              </BotaoOutline>
            </Animated.View>
          </View>
        )}

        {/* === ETAPA 6: Fingerprint === */}
        {etapa === 6 && (
          <View style={s.fullWidth}>
            <Header />
            <Text style={s.amountSmall}>MZN {valor}</Text>

            <Animated.View style={[s.cartaoBranco, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.textoPreto}>Toque no sensor</Text>

              <Animated.View style={{
                marginTop: 50,
                transform: [{ scale: fpEscala }],
                opacity: fpBrilho,
              }}>
                <View style={s.fpGlow} />
              </Animated.View>

              <View style={{ marginTop: -90 }}>
                <FingerprintIcone />
              </View>

              <BotaoPrincipal onPress={() => irPara(8)} estilo={{ marginTop: 60, backgroundColor: '#000' }}>
                <Text style={{ color: '#FFF', fontSize: 16, fontWeight: 'bold' }}>Confirmar</Text>
              </BotaoPrincipal>
            </Animated.View>
          </View>
        )}

        {/* === ETAPA 7: PIN === */}
        {etapa === 7 && (
          <View style={s.fullWidth}>
            <Header />
            <Text style={s.amountSmall}>MZN {valor}</Text>

            <Animated.View style={[s.cartaoBranco, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.textoPreto}>Número do celular</Text>
              <View style={s.campoInput}>
                <Text style={s.inputPlaceholder}>+258 84 XXX XXX</Text>
              </View>

              <Text style={[s.textoPreto, { marginTop: 24 }]}>Insira o PIN</Text>
              <View style={s.pinDots}>
                {[0,1,2,3].map(i => (
                  <View key={i} style={s.pinDot} />
                ))}
              </View>

              <BotaoPrincipal onPress={() => irPara(8)} estilo={{ marginTop: 30, backgroundColor: '#000' }}>
                <Text style={{ color: '#FFF', fontSize: 16, fontWeight: 'bold' }}>Confirmar</Text>
              </BotaoPrincipal>
            </Animated.View>
          </View>
        )}

        {/* === ETAPA 9: QR Code (NOVA TELA) === */}
        {etapa === 9 && (
          <View style={s.fullWidth}>
            <Header />
            <Text style={s.amountSmall}> MZN {valor}</Text>

            <Animated.View style={[s.cartaoBranco, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.textoPreto}>QR Code</Text>

              <View style={s.qrBox}>
                <QrCodeIcone />
              </View>

              <Text style={[s.textoPreto, { marginTop: 24, textAlign: 'center' }]}>
                Abra o app do M-Pesa
              </Text>

              <BotaoPrincipal onPress={() => irPara(8)} estilo={{ marginTop: 30, backgroundColor: '#000' }}>
                <Text style={{ color: '#FFF', fontSize: 16, fontWeight: 'bold' }}>Confirmar Pagamento</Text>
              </BotaoPrincipal>
            </Animated.View>
          </View>
        )}

        {/* === ETAPA 8: Confirmado === */}
        {etapa === 8 && (
          <View style={s.fullWidth}>
            <Header />
            <Animated.View style={[s.stageContent, {
              opacity: conteudoOpacidade,
              transform: [{ translateY: conteudoSlide }],
            }]}>
              <Text style={s.confirmadoTexto}>Confirmado</Text>

              <Animated.View style={{ transform: [{ scale: checkEscala }] }}>
                <View style={s.checkCircle}>
                  <Text style={s.checkIcon}>✓</Text>
                </View>
              </Animated.View>

              <Animated.View style={{
                opacity: valorOpacidade,
                transform: [{ translateY: valorSlide }],
                alignItems: 'center',
              }}>
                <Text style={s.amountDisplay}>
                  <Text style={s.currency}>MZN </Text>{valor}
                </Text>
              </Animated.View>

              <TouchableOpacity style={{ marginTop: 16 }}>
                <Text style={s.linkBranco}>Ver extrato</Text>
              </TouchableOpacity>

              <BotaoOutline onPress={() => irPara(2)} estilo={{ marginTop: 24 }}>
                Novo Pagamento
              </BotaoOutline>
            </Animated.View>
          </View>
        )}

      </Animated.View>
    </SafeAreaView>
  );
}

const s = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#EB3324',
  },
  telaWrapper: {
    flex: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  fullWidth: {
    width: '100%',
    flex: 1,
  },
  stageContent: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 24,
  },

  // Header
  header: {
    marginTop: 36,
    alignItems: 'center',
    marginBottom: 16,
  },
  logoText: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: 'bold',
    fontFamily: 'monospace',
  },

  // Splash
  centerBox: {
    alignItems: 'center',
    width: '100%',
  },
  logoGrande: {
    color: '#FFFFFF',
    fontSize: 36,
    fontWeight: 'bold',
    fontFamily: 'monospace',
  },
  logoSub: {
    color: '#FFFFFF',
    fontSize: 22,
    fontWeight: '300',
    fontFamily: 'monospace',
    opacity: 0.8,
  },

  // Labels e textos
  label: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 70,
    marginBottom: 8,
  },
  subLabel: {
    color: '#FFFFFF',
    fontSize: 14,
    alignSelf: 'flex-start',
    marginTop: 32,
    marginBottom: 12,
    opacity: 0.85,
  },
  amountDisplay: {
    color: '#FFFFFF',
    fontSize: 56,
    fontWeight: 'bold',
    marginBottom: 36,
    fontFamily: 'calibri',
  },
  amountSmall: {
    color: '#FFFFFF',
    fontSize: 38,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 16,
    fontFamily: 'monospace',
  },
  currency: {
    fontSize: 20,
  },

  // Botões
  mainButton: {
    backgroundColor: '#FFFFFF',
    paddingVertical: 18,
    alignItems: 'center',
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
    paddingHorizontal: 24,
  },
  outlineButton: {
    borderWidth: 2,
    borderColor: '#FFFFFF',
    backgroundColor: 'transparent',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 14,
  },
  buttonText: {
    color: '#EB3324',
    fontSize: 16,
    fontWeight: 'bold',
  },
  outlineButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
  },
  halfBtn: {
    width: '48%',
  },

  // Teclado numérico
  teclado: {
    width: '100%',
    marginBottom: 16,
  },
  tecladoLinha: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 10,
  },
  tecla: {
    width: '29%',
    height: 54,
    backgroundColor: 'rgba(255,255,255,0.12)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  teclaTexto: {
    color: '#FFFFFF',
    fontSize: 22,
    fontWeight: '500',
  },

  // Cartão branco (fingerprint / PIN / QR)
  cartaoBranco: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 36,
    borderTopRightRadius: 36,
    flex: 1,
    padding: 36,
    alignItems: 'center',
  },
  textoPreto: {
    color: '#000',
    fontSize: 17,
    fontWeight: 'bold',
  },
  campoInput: {
    width: '100%',
    borderBottomWidth: 2,
    borderBottomColor: '#CCC',
    paddingVertical: 12,
    marginTop: 8,
    marginBottom: 4,
  },
  inputPlaceholder: {
    color: '#AAA',
    fontSize: 20,
    fontFamily: 'monospace',
  },
  pinDots: {
    flexDirection: 'row',
    gap: 18,
    marginTop: 16,
  },
  pinDot: {
    width: 16,
    height: 16,
    borderRadius: 8,
    borderWidth: 2,
    borderColor: '#333',
    backgroundColor: 'transparent',
  },

  // Fingerprint
  fpContainer: {
    width: 72,
    height: 72,
    justifyContent: 'center',
    alignItems: 'center',
  },
  fpGlow: {
    width: 110,
    height: 110,
    borderRadius: 55,
    backgroundColor: '#EB3324',
  },

  // QR Code (Novo)
  qrBox: {
    marginTop: 40,
    padding: 16,
    backgroundColor: '#F5F5F5',
    borderRadius: 8,
  },
  qrWrapper: {
    flexDirection: 'column',
  },
  qrRow: {
    flexDirection: 'row',
  },
  qrCell: {
    width: 20,
    height: 20,
    backgroundColor: '#FFF',
  },
  qrCellBlack: {
    backgroundColor: '#000',
  },

  // Confirmado
  confirmadoTexto: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  checkCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#28a745',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
  },
  checkIcon: {
    color: '#FFF',
    fontSize: 36,
    fontWeight: 'bold',
  },
  linkBranco: {
    color: '#FFFFFF',
    textDecorationLine: 'underline',
    fontSize: 14,
    opacity: 0.85,
  },

  // Footer
  footer: {
    position: 'absolute',
    bottom: 30,
    alignSelf: 'center',
  },
  footerText: {
    color: '#FFFFFF',
    fontSize: 12,
    opacity: 0.5,
  },
});