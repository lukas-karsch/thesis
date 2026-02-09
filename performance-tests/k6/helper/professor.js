import {assertResponseIs201} from "./assert.js";

export function createProfessor(http, params, host) {
    const createProfessorResponse = http.post(`${host}/users/professor`, JSON.stringify(params), {
        headers: {
            "Content-Type": "application/json"
        }
    })
    assertResponseIs201(createProfessorResponse)
    const professorId = createProfessorResponse.json().data
    console.log(`Created professor with ID ${professorId}`)

    return professorId;
}
