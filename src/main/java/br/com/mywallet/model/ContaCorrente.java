package br.com.mywallet.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by brienze on 5/11/17.
 */
@Entity
@DiscriminatorValue(value = "1")
public class ContaCorrente extends Conta implements Serializable {


}
