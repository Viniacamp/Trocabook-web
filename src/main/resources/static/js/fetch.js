document.getElementById("pesquisa").addEventListener("input", function () {
    const titulo = this.value;
    const div = document.getElementById("resultadosPesquisa");

    div.innerHTML = "";

    if (titulo.length >= 1) {
        fetch(`/pesquisar?titulo=${encodeURIComponent(titulo)}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error("Falha ao encontrar livro");
                }

                return res.json();
            })
            .then(anuncios => {

                if (anuncios.length === 0) {
                    div.innerHTML = "<p>Nenhum livro encontrado</p>";
                } else {

                    anuncios.forEach(anuncio => {

                        const link = document.createElement("a");

                        // ID do anúncio
                        link.href = "/chat/" + anuncio.id;

                        const capa = document.createElement("img");
                        capa.src = anuncio.capa;
                        capa.className = "livro-capa";
                        capa.alt = "Capa do livro";

                        const texto = document.createElement("p");
                        texto.innerText = anuncio.titulo;

                        const vendedor = document.createElement("img");
                        vendedor.src = anuncio.fotoPerfil;
                        vendedor.className = "perfil-foto";
                        vendedor.alt = "Foto do usuário";

                        link.appendChild(capa);
                        link.appendChild(texto);
                        link.appendChild(vendedor);

                        div.appendChild(link);
                    });
                }
            })
            .catch(erro => {
                div.innerHTML =
                    `<p>Ocorreu um erro ao buscar o livro: ${erro.message}</p>`;
            });
    }
});