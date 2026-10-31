import axios from "../customAxiosConfig/CustomAxiosConfig";

const DeleteHobbyService = async (id) => {
  try {
    return await axios.delete(`/hobbies/${id}`);
  } catch (err) {
    if (err.response && err.response.status === 404) {
      try {
        return await axios.delete(`/offers/${id}`);
      } catch (fallbackErr) {
        throw fallbackErr;
      }
    }
    throw err;
  }
};

export default DeleteHobbyService;
