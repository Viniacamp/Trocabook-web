const campoPesquisa = document.getElementById("pesquisa");
const div = document.getElementById("resultadosPesquisa");

let timeoutPesquisa;
let timeoutInteracao;

campoPesquisa.addEventListener("input", function () {

    const titulo = this.value.trim();

    div.innerHTML = "";

    clearTimeout(timeoutPesquisa);
    clearTimeout(timeoutInteracao);

    if (titulo.length < 2) {
        return;
    }

    // Aguarda um pouco antes de buscar
    timeoutPesquisa = setTimeout(() => {
        pesquisar(titulo);
    }, 600);
});

function pesquisar(titulo) {

    fetch(`/pesquisar?titulo=${encodeURIComponent(titulo)}`)
        .then(res => {

            if (!res.ok) {
                throw new Error("Falha ao encontrar livro");
            }

            return res.json();
        })
        .then(anuncios => {

            div.innerHTML = "";

            if (anuncios.length === 0) {

                div.innerHTML = "<p>Nenhum livro encontrado</p>";

            } else {

                // Aguarda para considerar o termo como uma pesquisa real
                clearTimeout(timeoutInteracao);

                timeoutInteracao = setTimeout(() => {

                    if (campoPesquisa.value.trim() === titulo) {
                        registrarInteracaoPesquisa(titulo);
                    }

                }, 2000);

                anuncios.forEach(anuncio => {

                    const link = document.createElement("a");
                    link.href = "/anuncios/" + anuncio.id;

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

function registrarInteracaoPesquisa(termo) {

    fetch(`/pesquisar/interacao?termo=${encodeURIComponent(termo)}`, {
        method: "POST"
    })
        .then(res => {
            if (!res.ok) {
                throw new Error(
                    `Erro ${res.status} ao registrar interação`
                );
            }
        })
        .catch(erro => {
            console.error(
                "Erro ao registrar interação de pesquisa:",
                erro
            );
        });
}