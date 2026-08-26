DESPERTADOR PROFESSOR MOREIRA — ANDROID NATIVO

O projeto foi criado em Java para Android nativo e usa AlarmManager.setAlarmClock(),
que é muito mais apropriado para despertadores do que um PWA.

RECURSOS
- Horário configurável (inicial 05:00)
- Alarme diário
- Toca com som de alarme do próprio aparelho
- Vibração
- Tela de alarme sobre a tela bloqueada
- Reagenda o próximo dia automaticamente
- Reagenda após reiniciar o celular
- Botão de teste
- Botão para desativar
- Identidade Curso de Contabilidade Professor Moreira

COMO GERAR O APK NO ANDROID STUDIO
1. Instale o Android Studio.
2. Abra a pasta DespertadorProfessorMoreiraAndroid.
3. Aguarde o Gradle Sync.
4. Se o Android Studio pedir SDK 35, permita a instalação.
5. Vá em Build > Build Bundle(s) / APK(s) > Build APK(s).
6. O APK de teste será criado em:
   app/build/outputs/apk/debug/app-debug.apk

PARA DISTRIBUIR AOS ALUNOS
Para um APK definitivo:
1. Build > Generate Signed Bundle / APK.
2. Escolha APK.
3. Crie uma keystore e guarde a senha em local seguro.
4. Escolha release.
5. Gere o app-release.apk.

PERMISSÕES
No Android 12 ou superior, ao ativar o alarme pela primeira vez, o sistema pode pedir
autorização para "Alarmes e lembretes".
No Android 13 ou superior, permita notificações.

OBSERVAÇÃO IMPORTANTE
Em alguns aparelhos com modos agressivos de economia de bateria (Xiaomi, Samsung etc.),
pode ser útil permitir que o aplicativo funcione sem restrições de bateria.

O ambiente desta conversa não possui Android SDK/Gradle, portanto não foi possível
compilar e assinar o APK aqui. O código-fonte está pronto para Android Studio.
