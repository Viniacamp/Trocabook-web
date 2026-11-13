const campoPesquisa = document.getElementById("pesquisa");
const div = document.getElementById("resultadosPesquisa");

let timeoutPesquisa;
let timeoutInteracao;

if (campoPesquisa && div) {
    campoPesquisa.addEventListener("input", function () {
        const titulo = this.value.trim();

        clearTimeout(timeoutPesquisa);
        clearTimeout(timeoutInteracao);

        if (titulo.length < 2) {
            div.innerHTML = "";
            div.style.display = "none";
            return;
        }

        // Feedback visual imediato enquanto aguarda o debounce e a consulta da IA
        div.style.display = "block";
        div.innerHTML = `
            <p style="padding: 12px; text-align: center; color: #6c63ff; font-weight: 500; margin: 0;">
                <i class="fas fa-spinner fa-spin" style="margin-right: 6px;"></i>
                Buscando livros...
            </p>
        `;

        timeoutPesquisa = setTimeout(() => {
            pesquisar(titulo);
        }, 600);
    });

    document.addEventListener("click", function (event) {
        if (!campoPesquisa.contains(event.target) && !div.contains(event.target)) {
            div.style.display = "none";
        }
    });

    campoPesquisa.addEventListener("focus", function () {
        if (div.innerHTML.trim() !== "") {
            div.style.display = "block";
        }
    });
}

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
            div.style.display = "block";

            if (anuncios.length === 0) {
                div.innerHTML = "<p>Nenhum livro encontrado</p>";
            } else {
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
            div.style.display = "block";
            div.innerHTML = `<p>Ocorreu um erro ao buscar o livro: ${erro.message}</p>`;
        });
}

function registrarInteracaoPesquisa(termo) {
    fetch(`/pesquisar/interacao?termo=${encodeURIComponent(termo)}`, {
        method: "POST"
    })
        .then(res => {
            if (!res.ok) {
                throw new Error(`Erro ${res.status} ao registrar interação`);
            }
        })
        .catch(erro => {
            console.error("Erro ao registrar interação de pesquisa:", erro);
        });
}