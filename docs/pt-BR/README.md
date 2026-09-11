# F21 Noir — guia em português

O F21 Noir é um launcher pequeno e totalmente offline para o Qin F21 Pro com Android 11.
Ele não remove nem substitui o Launcher3 original.

## Instalação segura

1. Confirme que somente o F21 correto está selecionado no ADB.
2. Instale o APK usando sempre `adb -s SERIAL_DO_F21 install -r APK`.
3. Pressione a tecla Home e escolha **F21 Noir**.
4. Para voltar, segure Menu, abra **Launcher padrão** e selecione Launcher3.

## Teclas

- Direcional: move o foco.
- Na Home, o foco dá a volta nas bordas para que toda direção produza movimento visível.
- Centro: abre o item.
- Menu: mostra todos os aplicativos.
- Deslizar para cima na Home: também mostra todos os aplicativos.
- Deslizar para baixo nos aplicativos: retorna à Home.
- Deslizar para os lados nos aplicativos: muda de página.
- Menu longo: abre os ajustes.
- Toque no relógio: também abre os ajustes.
- Chamar: abre o telefone.
- Número curto: abre o discador com o número.
- Número longo: executa o atalho configurado.
- Voltar: fecha a tela atual ou retorna à home.

## Privacidade

Não existe permissão de Internet, telemetria, propaganda, conta, sincronização ou Google Play
Services. A lista de apps ocultos, favoritos e atalhos fica somente no próprio aparelho.

## Projetos relacionados

Para combinar o launcher com o painel de notificações e os detalhes visuais preto/âmbar, veja o
[F21 Noir Theme](https://github.com/elmirok/f21-noir-theme). Para recuperar o aparelho e reinstalar
o firmware compatível, veja o [F21 Rescue](https://github.com/elmirok/f21-rescue).
